package com.example.composeapp.permission

import android.Manifest
import android.os.Build

object PermissionRequest {

    fun permissionsFor(type: PermissionType): Array<String> {
        return when (type) {
            PermissionType.BLE ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                } else {
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                }

            PermissionType.CAMERA ->
                arrayOf(Manifest.permission.CAMERA)

            PermissionType.LOCATION ->
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

            PermissionType.NOTIFICATIONS ->
                if (Build.VERSION.SDK_INT >= 33) {
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                } else emptyArray()
        }
    }
}
