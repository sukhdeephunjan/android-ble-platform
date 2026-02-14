package com.example.composeapp.permission.flow

import com.example.composeapp.permission.PermissionType

data class PermissionFlowState(
    val currentPermission: PermissionType? = null,
    val isComplete: Boolean = false
)