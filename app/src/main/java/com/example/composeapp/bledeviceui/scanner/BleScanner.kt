package com.example.composeapp.bledeviceui.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
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

            // Read name from advertising packet first — result.device.name
            // can be null for iOS peripherals on first discovery.
            val name = result.scanRecord?.deviceName
                ?.takeIf { it.isNotBlank() }
                ?: result.device.name?.takeIf { it.isNotBlank() }
                ?: "Unknown Device"

            val device = UiBleDevice(
                id = result.device.address,
                name = name,
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

    /**
     * Starts a hardware-filtered BLE scan.
     *
     * @param allowedServiceUuids Only devices advertising at least one of these
     *   service UUIDs will be delivered by the OS. Passing an empty list falls
     *   back to an unfiltered scan (not recommended for production).
     */
    @SuppressLint("MissingPermission")
    fun startScan(allowedServiceUuids: List<String> = emptyList()) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _scanError.value = "Bluetooth is not available or disabled"
            return
        }
        seenDevices.clear()
        _scanResults.value = emptyList()
        _scanError.value = null

        val scanner = bluetoothAdapter.bluetoothLeScanner
            ?: run { _scanError.value = "BLE scanner not available"; return }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        if (allowedServiceUuids.isEmpty()) {
            // No filter — returns every advertising device (fallback only)
            scanner.startScan(null, settings, scanCallback)
        } else {
            // Hardware filter — OS delivers ONLY devices advertising one of these UUIDs
            val filters = allowedServiceUuids.map { uuid ->
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(uuid))
                    .build()
            }
            scanner.startScan(filters, settings, scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }
}
