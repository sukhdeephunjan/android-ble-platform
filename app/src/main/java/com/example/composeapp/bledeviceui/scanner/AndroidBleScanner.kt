package com.example.composeapp.bledeviceui.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.example.composeapp.bledeviceui.model.ScanResultWrapper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidBleScanner(
    private val bluetoothAdapter: BluetoothAdapter
) : BleScanner {

    private val scanner get() = bluetoothAdapter.bluetoothLeScanner
    private var callback: ScanCallback? = null

    override fun scan(): Flow<ScanResultWrapper> = callbackFlow {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(type: Int, result: ScanResult) {
                trySend(
                    ScanResultWrapper(
                        deviceId = result.device.address,
                        deviceName = result.device.name,
                        serviceUuids = result.scanRecord
                            ?.serviceUuids
                            ?.map { it.uuid }
                            .orEmpty()
                    )
                )
            }
        }

        callback = scanCallback
        scanner.startScan(scanCallback)

        awaitClose {
            scanner.stopScan(scanCallback)
        }
    }

    override fun stop() {
        callback?.let { scanner.stopScan(it) }
    }
}