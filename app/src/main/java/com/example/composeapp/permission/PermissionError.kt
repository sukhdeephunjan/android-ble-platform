package com.example.composeapp.permission

import com.example.composeapp.R

sealed class PermissionError(
    val messageRes: Int
) {
    object Denied :
        PermissionError(R.string.permission_denied)

    object PermanentlyDenied :
        PermissionError(R.string.permission_permanently_denied)
}

