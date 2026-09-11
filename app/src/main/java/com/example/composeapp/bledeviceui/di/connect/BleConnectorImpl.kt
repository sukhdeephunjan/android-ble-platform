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
private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

@Singleton
class BleConnectorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    profileProvider: BleProfileProvider
) : BleConnector {

    private var gatt: BluetoothGatt? = null
    private var connectContinuation: CancellableContinuation<Result<Unit>>? = null

    /** Characteristics still waiting for their CCCD write (written one at a time). */
    private val pendingNotifications = ArrayDeque<BluetoothGattCharacteristic>()

    private val _dataUpdates = MutableSharedFlow<BleParsedData>(
        replay = 1,
        extraBufferCapacity = 32
    )
    override val dataUpdates: SharedFlow<BleParsedData> = _dataUpdates.asSharedFlow()

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
    override suspend fun connect(deviceId: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->

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

            gatt = device.connectGatt(context, false, gattCallback)

            cont.invokeOnCancellation { closeGatt() }
        }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        closeGatt()
    }

    // ── GATT callback ────────────────────────────────────────────────────────

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                resumeOnce(Result.failure(Exception("GATT error: $status")))
                return
            }
            when (newState) {
                // Do NOT resume yet — wait for service discovery to complete
                BluetoothGatt.STATE_CONNECTED -> gatt.discoverServices()
                BluetoothGatt.STATE_DISCONNECTED ->
                    resumeOnce(Result.failure(Exception("Disconnected")))
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                resumeOnce(Result.failure(Exception("Service discovery failed: $status")))
                return
            }
            enqueueNotifications(gatt)
            // Connection is usable now — notifications keep enabling in background
            resumeOnce(Result.success(Unit))
            writeNextDescriptor()
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            // Proceed with the next characteristic regardless of individual failures
            writeNextDescriptor()
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
    private fun enqueueNotifications(gatt: BluetoothGatt) {
        pendingNotifications.clear()
        gatt.services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                val def = characteristicDefs[characteristic.uuid.toString().uppercase()]
                if (def != null && def.enableOnConnect) {
                    pendingNotifications.addLast(characteristic)
                }
            }
        }
        Log.d(TAG, "Characteristics to enable: ${pendingNotifications.size}")
    }

    /**
     * Enables the next queued characteristic. Only one descriptor write may be
     * in flight at a time on Android, so the queue advances in `onDescriptorWrite`.
     */
    @SuppressLint("MissingPermission")
    private fun writeNextDescriptor() {
        val g = gatt ?: return
        val characteristic = pendingNotifications.removeFirstOrNull() ?: return

        g.setCharacteristicNotification(characteristic, true)

        val descriptor = characteristic.getDescriptor(CCCD_UUID) ?: run {
            // No CCCD on this characteristic — skip to the next one
            writeNextDescriptor()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeDescriptor(descriptor, value)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = value
            @Suppress("DEPRECATION")
            g.writeDescriptor(descriptor)
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
    private fun closeGatt() {
        pendingNotifications.clear()
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        connectContinuation = null
    }
}
