package com.example.composeapp.bledeviceui.events

import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.bledeviceui.uistate.LiveDataTab

sealed class ScanEvent {
    data class ProfileSelected(val profileId: String?) : ScanEvent()
    data class DeviceClicked(val device: UiBleDevice) : ScanEvent()
    data class TabSelected(val tab: LiveDataTab) : ScanEvent()
    object StartScan : ScanEvent()
    object StopScan : ScanEvent()
    object DismissDialog : ScanEvent()
    object Rescan : ScanEvent()
}
