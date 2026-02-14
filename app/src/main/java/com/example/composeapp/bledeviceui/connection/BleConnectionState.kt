package com.example.composeapp.bledeviceui.connection

import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.bledeviceui.uistate.UiError

sealed interface BleConnectionState {

    /** No scan, no connection */
    object Idle : BleConnectionState

    /** Scanning for devices */
    object Scanning : BleConnectionState

    /** User selected a device, connecting */
    data class Connecting(
        val device: UiBleDevice
    ) : BleConnectionState

    /** Connected and ready for service discovery */
    data class Connected(
        val device: UiBleDevice
    ) : BleConnectionState

    /** Any failure during scan or connect */
    data class Error(
        val error: UiError
    ) : BleConnectionState
}
