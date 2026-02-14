package com.example.composeapp.permission.permissionstate

import com.example.composeapp.permission.PermissionError
import com.example.composeapp.permission.PermissionType

data class PermissionUiState(
    val permissionType: PermissionType,
    val state: PermissionState = PermissionState.Requested,
    val error: PermissionError? = null
)
