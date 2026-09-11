package com.example.composeapp.bledeviceui.events

import com.example.composeapp.bledeviceui.model.UiBleDevice

sealed class ScanEvent {
    data class ProfileSelected(val profileId: String?) : ScanEvent()
    data class DeviceClicked(val device: UiBleDevice) : ScanEvent()
    object StartScan : ScanEvent()
    object StopScan : ScanEvent()
    object DismissDialog : ScanEvent()
    object Rescan : ScanEvent()
}
