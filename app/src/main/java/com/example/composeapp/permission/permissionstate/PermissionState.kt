package com.example.composeapp.permission.permissionstate

sealed class PermissionState {
    object Requested : PermissionState()
    object Granted : PermissionState()
    object Denied : PermissionState()
    object PermanentlyDenied : PermissionState()
}