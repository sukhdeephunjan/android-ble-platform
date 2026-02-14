package com.example.composeapp.bledeviceui.events

import com.example.composeapp.bledeviceui.model.UiBleDevice

sealed interface ConnectEvent {
    data class Connect(val device: UiBleDevice) : ConnectEvent
    object Disconnect : ConnectEvent
    object Retry : ConnectEvent
}