package com.example.composeapp.bledeviceui.uistate

import com.example.composeapp.bledeviceui.model.UiBleDevice

data class ScanUiState(
    val isScanning: Boolean = false,
    val allDevices: List<UiBleDevice> = emptyList(),
    val visibleDevices: List<UiBleDevice> = emptyList(),
    val selectedProfileId: String? = null
)
