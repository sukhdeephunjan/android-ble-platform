package com.example.composeapp.bledeviceui.di.connect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import com.example.composeapp.BleConnector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class BleConnectorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleConnector {

    private var gatt: BluetoothGatt? = null

    override suspend fun connect(deviceId: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->

            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: run {
                    cont.resume(Result.failure(IllegalStateException("Bluetooth not supported")))
                    return@suspendCancellableCoroutine
                }

            val device: BluetoothDevice = try {
                adapter.getRemoteDevice(deviceId)
            } catch (e: IllegalArgumentException) {
                cont.resume(Result.failure(e))
                return@suspendCancellableCoroutine
            }

            gatt = device.connectGatt(
                context,
                false,
                object : BluetoothGattCallback() {

                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            cont.resume(Result.failure(Exception("GATT error: $status")))
                            return
                        }

                        if (newState == BluetoothGatt.STATE_CONNECTED) {
                            cont.resume(Result.success(Unit))
                        } else {
                            cont.resume(Result.failure(Exception("Disconnected")))
                        }
                    }
                }
            )

            cont.invokeOnCancellation {
                gatt?.disconnect()
                gatt?.close()
                gatt = null
            }
        }

    override fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }
}
