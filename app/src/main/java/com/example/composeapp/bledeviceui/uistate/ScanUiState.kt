package com.example.composeapp.bledeviceui.uistate

import com.example.blelib.bleCore.BleProfile
import com.example.composeapp.bledeviceui.model.UiBleDevice

data class ScanUiState(
    val allDevices: List<UiBleDevice> = emptyList(),
    val filteredDevices: List<UiBleDevice> = emptyList(),
    val selectedProfileId: String? = null,
    val profiles: List<BleProfile> = emptyList(),
    val isScanning: Boolean = false,
    val errorMessage: String? = null,
    // Dialog & connection state
    val showDeviceDialog: Boolean = false,
    val connectionStateLabel: String = "",
    val connectedDevice: UiBleDevice? = null,
    val connectionAttempt: Int = 0,
    // Live data received from the connected peripheral
    val heartRateBpm: Int? = null,
    val temperatureCelsius: Float? = null
)
