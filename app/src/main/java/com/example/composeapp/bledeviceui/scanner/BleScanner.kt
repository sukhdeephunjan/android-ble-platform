package com.example.composeapp.bledeviceui.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.ParcelUuid
import com.example.composeapp.bledeviceui.model.UiBleDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private val _scanResults = MutableStateFlow<List<UiBleDevice>>(emptyList())
    val scanResults: StateFlow<List<UiBleDevice>> = _scanResults.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    // Deduplicate by MAC address
    private val seenDevices = mutableMapOf<String, UiBleDevice>()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val serviceUuids = result.scanRecord
                ?.serviceUuids
                ?.map { it.toString().uppercase() }
                ?: emptyList()

            val device = UiBleDevice(
                id = result.device.address,
                name = result.device.name?.takeIf { it.isNotBlank() } ?: "Unknown Device",
                rssi = result.rssi,
                serviceUuids = serviceUuids
            )
            seenDevices[device.id] = device
            _scanResults.value = seenDevices.values.sortedByDescending { it.rssi }
        }

        override fun onScanFailed(errorCode: Int) {
            _scanError.value = "BLE scan failed (code $errorCode)"
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _scanError.value = "Bluetooth is not available or disabled"
            return
        }
        seenDevices.clear()
        _scanResults.value = emptyList()
        _scanError.value = null
        bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
            ?: run { _scanError.value = "BLE scanner not available" }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }
}

