package com.example.composeapp.bledeviceui.model

import java.util.UUID

data class ScanResultWrapper(
    val deviceId: String,
    val deviceName: String?,
    val serviceUuids: List<UUID>
)