package com.example.composeapp.bledeviceui.uistate

import com.example.blelib.bleCore.BleProfile
import com.example.composeapp.bledeviceui.model.UiBleDevice

data class ScanUiState(
    val allDevices: List<UiBleDevice> = emptyList(),
    val filteredDevices: List<UiBleDevice> = emptyList(),
    val selectedProfileId: String? = null,
    val profiles: List<BleProfile> = emptyList(),
    val isScanning: Boolean = false,
    val errorMessage: String? = null
)
