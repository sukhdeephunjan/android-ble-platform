package com.example.composeapp.permission

import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat

object PermissionMapper {
    fun map(
        context: Context,
        result: Map<String, Boolean>
    ): PermissionResult {
        val activity = context as? Activity ?: return PermissionResult(
            granted = false,
            permanentlyDenied = false
        )

        val allGranted = result.values.all { it }
        val permanentlyDenied = result.any { (permission, granted) ->
            !granted &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        permission
                    )
        }

        return PermissionResult(
            granted = allGranted,
            permanentlyDenied = permanentlyDenied
        )
    }
}
