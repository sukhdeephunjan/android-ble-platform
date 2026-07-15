package com.example.composeapp.permission.permissionstate

sealed class PermissionState {
    object Requested : PermissionState()
    object Denied : PermissionState()
    object Rationale : PermissionState()
    object PermanentlyDenied : PermissionState()
    object Granted : PermissionState()
}

