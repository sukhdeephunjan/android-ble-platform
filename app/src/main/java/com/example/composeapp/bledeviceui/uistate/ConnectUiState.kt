package com.example.composeapp.bledeviceui.uistate

import com.example.composeapp.bledeviceui.connection.BleConnectionState

data class ConnectUiState(
    val state: BleConnectionState = BleConnectionState.Idle
)