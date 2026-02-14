package com.example.composeapp.permission.ui


import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.composeapp.permission.PermissionRequest
import com.example.composeapp.permission.PermissionType

object PermissionChecker {

    fun isGranted(
        context: Context,
        permissionType: PermissionType
    ): Boolean {
        return PermissionRequest
            .permissionsFor(permissionType)
            .all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
    }
}
