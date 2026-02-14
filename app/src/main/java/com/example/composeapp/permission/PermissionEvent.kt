package com.example.composeapp.permission

sealed class PermissionEvent {
    object PermissionRequest : PermissionEvent()
    object PermissionGranted : PermissionEvent()
    object PermissionDenied : PermissionEvent()
    object PermissionPermanentlyDenied : PermissionEvent()
}
