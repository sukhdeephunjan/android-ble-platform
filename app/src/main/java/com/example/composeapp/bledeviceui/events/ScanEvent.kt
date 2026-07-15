package com.example.composeapp.bledeviceui.events

sealed class ScanEvent {
    data class ProfileSelected(val profileId: String?) : ScanEvent()
    data class DeviceClicked(val deviceId: String) : ScanEvent()
    object StartScan : ScanEvent()
    object StopScan : ScanEvent()
}

