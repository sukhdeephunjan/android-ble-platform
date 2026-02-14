package com.example.composeapp.permission.flow

import com.example.composeapp.permission.PermissionType

data class PermissionStep(
    val type: PermissionType,
    val mandatory: Boolean = true
)