package com.example.composeapp.permission

data class PermissionResult(
    val granted: Boolean,
    val permanentlyDenied: Boolean
)