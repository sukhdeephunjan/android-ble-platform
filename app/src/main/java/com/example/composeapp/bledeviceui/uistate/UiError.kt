package com.example.composeapp.bledeviceui.uistate

sealed class UiError {
    data class Message(val text: String) : UiError()
    object BluetoothDisabled : UiError()
    object PermissionDenied : UiError()
    object DeviceNotFound : UiError()
    object ConnectionFailed : UiError()
}