package com.example.composeapp.bledeviceui.di.connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.blelib.bleCore.BleCharacteristicDef
import com.example.blelib.bleCore.BleProfileProvider
import com.example.blelib.bleCore.CharacteristicProperty
import com.example.blelib.bleCore.Uuids
import com.example.composeapp.BleConnector
import com.example.composeapp.bledeviceui.model.BleParsedData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "BleConnector"

/** Client Characteristic Configuration Descriptor — used to turn notifications on. */
private val CCCD_UUID: UUID = UUID.fromString(Uuids.CCCD)

private sealed class GattOperation {
    data class EnableNotification(val characteristic: BluetoothGattCharacteristic) : GattOperation()
    data class ReadCharacteristic(val characteristic: BluetoothGattCharacteristic) : GattOperation()
    data class WriteCharacteristic(val characteristic: BluetoothGattCharacteristic, val value: ByteArray) : GattOperation() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as WriteCharacteristic
            return (characteristic == other.characteristic) && value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            var result = characteristic.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }
}

@Singleton
class BleConnectorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    profileProvider: BleProfileProvider,
) : BleConnector {

    private var gatt: BluetoothGatt? = null
    private var connectContinuation: CancellableContinuation<Result<Unit>>? = null

    /** Operations waiting to be executed sequentially on BluetoothGatt. */
    private val pendingOperations = ArrayDeque<GattOperation>()

    private val _dataUpdates = MutableSharedFlow<BleParsedData>(
        replay = 1,
        extraBufferCapacity = 32
    )
    override val dataUpdates: SharedFlow<BleParsedData> = _dataUpdates.asSharedFlow()

    private val _disconnections = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    override val disconnections: SharedFlow<Unit> = _disconnections.asSharedFlow()

    /** True once service discovery has completed — i.e. the link was usable. */
    @Volatile
    private var isEstablished = false

    /** Set while [disconnect] / [closeGatt] tears the link down on purpose. */
    @Volatile
    private var intentionalDisconnect = false

    /** characteristic UUID (uppercase) → definition (holds the parser). */
    private val characteristicDefs: Map<String, BleCharacteristicDef> =
        profileProvider.provideProfiles()
            .flatMap { it.services }
            .flatMap { it.characteristics }
            .associateBy { it.uuid.uppercase() }

    private val bluetoothAdapter by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    // ── Connect ──────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    override suspend fun connect(deviceId: String, autoConnect: Boolean): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            closeGatt()

            isEstablished = false
            intentionalDisconnect = false
            connectContinuation = cont

            val adapter = bluetoothAdapter ?: run {
                resumeOnce(Result.failure(IllegalStateException("Bluetooth not supported")))
                return@suspendCancellableCoroutine
            }

            val device: BluetoothDevice = try {
                adapter.getRemoteDevice(deviceId)
            } catch (e: IllegalArgumentException) {
                resumeOnce(Result.failure(e))
                return@suspendCancellableCoroutine
            }

            Log.d(TAG, "connectGatt($deviceId, autoConnect=$autoConnect)")
            gatt = device.connectGatt(context, autoConnect, gattCallback, BluetoothDevice.TRANSPORT_LE)

            cont.invokeOnCancellation { closeGatt() }
        }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        intentionalDisconnect = true
        isEstablished = false
        closeGatt()
    }

    @SuppressLint("MissingPermission")
    override suspend fun readCharacteristic(serviceUuid: String, characteristicUuid: String): Boolean {
        val g = gatt ?: return false
        val sUuid = UUID.fromString(serviceUuid)
        val cUuid = UUID.fromString(characteristicUuid)
        val service = g.getService(sUuid) ?: return false
        val characteristic = service.getCharacteristic(cUuid) ?: return false

        val isFirst: Boolean
        synchronized(pendingOperations) {
            isFirst = pendingOperations.isEmpty()
            pendingOperations.addLast(GattOperation.ReadCharacteristic(characteristic))
        }
        if (isFirst) {
            executeNextOperation()
        }
        return true
    }

    @SuppressLint("MissingPermission")
    override suspend fun writeCharacteristic(
        serviceUuid: String,
        characteristicUuid: String,
        value: ByteArray
    ): Boolean {
        val g = gatt ?: return false
        val sUuid = UUID.fromString(serviceUuid)
        val cUuid = UUID.fromString(characteristicUuid)
        val service = g.getService(sUuid) ?: return false
        val characteristic = service.getCharacteristic(cUuid) ?: return false

        val isFirst: Boolean
        synchronized(pendingOperations) {
            isFirst = pendingOperations.isEmpty()
            pendingOperations.addLast(GattOperation.WriteCharacteristic(characteristic, value))
        }
        if (isFirst) {
            executeNextOperation()
        }
        return true
    }

    override suspend fun writeHeartRateControlPoint(controlValue: Byte): Boolean {
        return writeCharacteristic(
            Uuids.HEART_RATE_SERVICE,
            Uuids.HEART_RATE_CONTROL_POINT,
            byteArrayOf(controlValue)
        )
    }

    // ── GATT callback ────────────────────────────────────────────────────────

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                if (isEstablished) {
                    notifyLinkLost()
                } else {
                    resumeOnce(Result.failure(Exception("GATT error: $status")))
                }
                return
            }
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    val started = gatt.discoverServices()
                    if (!started) {
                        Log.w(TAG, "discoverServices() could not be started")
                        resumeOnce(Result.failure(Exception("Service discovery could not start")))
                    }
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    if (isEstablished) {
                        notifyLinkLost()
                    } else {
                        resumeOnce(Result.failure(Exception("Disconnected")))
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                resumeOnce(Result.failure(Exception("Service discovery failed: $status")))
                return
            }
            Log.d(TAG, "Services discovered: ${gatt.services.joinToString { it.uuid.toString() }}")
            enqueuePendingOperations(gatt)
            isEstablished = true
            resumeOnce(Result.success(Unit))
            executeNextOperation()
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            executeNextOperation()
        }

        // API 33+
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                emitParsed(characteristic.uuid, value)
            } else {
                Log.w(TAG, "Read failed for ${characteristic.uuid}: status $status")
            }
            executeNextOperation()
        }

        // API < 33
        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    characteristic.value?.let { emitParsed(characteristic.uuid, it) }
                } else {
                    Log.w(TAG, "Read failed for ${characteristic.uuid}: status $status")
                }
                executeNextOperation()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully: ${characteristic.uuid}")
            } else {
                Log.w(TAG, "Write failed for ${characteristic.uuid}: status $status")
            }
            executeNextOperation()
        }

        // API 33+
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            emitParsed(characteristic.uuid, value)
        }

        // API < 33
        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                characteristic.value?.let { emitParsed(characteristic.uuid, it) }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Collects every discovered characteristic that is marked `enableOnConnect`. */
    @Synchronized
    private fun enqueuePendingOperations(gatt: BluetoothGatt) {
        pendingOperations.clear()
        gatt.services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                val def = characteristicDefs[characteristic.uuid.toString().uppercase()]
                if (def != null && def.enableOnConnect) {
                    val isReadInProfile = def.properties.contains(CharacteristicProperty.READ)
                    val isNotifyInProfile = def.properties.contains(CharacteristicProperty.NOTIFY) ||
                            def.properties.contains(CharacteristicProperty.INDICATE)

                    if (isReadInProfile) {
                        pendingOperations.addLast(GattOperation.ReadCharacteristic(characteristic))
                    }
                    if (isNotifyInProfile) {
                        pendingOperations.addLast(GattOperation.EnableNotification(characteristic))
                    }
                }
            }
        }
        Log.d(TAG, "Operations to execute: ${pendingOperations.size}")
    }

    @Synchronized
    @SuppressLint("MissingPermission")
    private fun executeNextOperation() {
        val g = gatt ?: run {
            pendingOperations.clear()
            return
        }
        val op = pendingOperations.removeFirstOrNull() ?: return

        when (op) {
            is GattOperation.EnableNotification -> {
                val characteristic = op.characteristic
                val setNotifySuccess = g.setCharacteristicNotification(characteristic, true)
                if (!setNotifySuccess) {
                    Log.w(TAG, "setCharacteristicNotification failed for ${characteristic.uuid}")
                }

                val descriptor = characteristic.getDescriptor(CCCD_UUID) ?: run {
                    Log.w(TAG, "No CCCD found for ${characteristic.uuid}")
                    executeNextOperation()
                    return
                }

                val def = characteristicDefs[characteristic.uuid.toString().uppercase()]
                val supportsIndicate =
                    def?.properties?.contains(CharacteristicProperty.INDICATE) == true &&
                            (characteristic.properties and
                                    BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0

                val value = if (supportsIndicate) {
                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                } else {
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                }

                val writeStarted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    g.writeDescriptor(descriptor, value) == android.bluetooth.BluetoothStatusCodes.SUCCESS
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = value
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(descriptor)
                }

                if (!writeStarted) {
                    Log.w(TAG, "writeDescriptor failed to start for ${characteristic.uuid}")
                    executeNextOperation()
                }
            }

            is GattOperation.ReadCharacteristic -> {
                val characteristic = op.characteristic
                @Suppress("DEPRECATION")
                val readStarted = g.readCharacteristic(characteristic)

                if (!readStarted) {
                    Log.w(TAG, "readCharacteristic failed to start for ${characteristic.uuid}")
                    executeNextOperation()
                }
            }

            is GattOperation.WriteCharacteristic -> {
                val characteristic = op.characteristic
                val writeStarted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    g.writeCharacteristic(
                        characteristic,
                        op.value,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    ) == android.bluetooth.BluetoothStatusCodes.SUCCESS
                } else {
                    @Suppress("DEPRECATION")
                    characteristic.value = op.value
                    @Suppress("DEPRECATION")
                    g.writeCharacteristic(characteristic)
                }

                if (!writeStarted) {
                    Log.w(TAG, "writeCharacteristic failed to start for ${characteristic.uuid}")
                    executeNextOperation()
                }
            }
        }
    }

    /** Parses raw bytes with the profile parser and publishes the result. */
    private fun emitParsed(uuid: UUID, bytes: ByteArray) {
        val key = uuid.toString().uppercase()
        val parser = characteristicDefs[key]?.parser ?: return
        val parsed = runCatching { parser.parse(bytes) }
            .onFailure { Log.w(TAG, "Parse failed for $key", it) }
            .getOrNull() ?: return

        _dataUpdates.tryEmit(BleParsedData(characteristicUuid = key, value = parsed))
    }

    private fun resumeOnce(result: Result<Unit>) {
        connectContinuation?.takeIf { it.isActive }?.resume(result)
        connectContinuation = null
    }

    @SuppressLint("MissingPermission")
    private fun notifyLinkLost() {
        if (intentionalDisconnect) return
        if (!isEstablished) return

        Log.w(TAG, "Link lost — notifying observers")
        isEstablished = false
        closeGatt()
        _disconnections.tryEmit(Unit)
    }

    @Synchronized
    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        pendingOperations.clear()
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        connectContinuation = null
    }
}
