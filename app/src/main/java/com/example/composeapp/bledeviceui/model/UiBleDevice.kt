package com.example.composeapp.bledeviceui.model

data class UiBleDevice(
    val id: String,
    val name: String,
    val rssi: Int = 0,
    val isConnected: Boolean = false,
    val serviceUuids: List<String> = emptyList()
)

