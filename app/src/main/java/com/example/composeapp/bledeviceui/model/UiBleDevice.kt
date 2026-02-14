package com.example.composeapp.bledeviceui.model

data class UiBleDevice(
    val id: String,          // usually MAC or generated ID
    val name: String,
    val profileIds: Set<String> = emptySet()
)