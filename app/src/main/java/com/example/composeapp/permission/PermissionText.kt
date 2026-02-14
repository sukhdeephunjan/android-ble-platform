package com.example.composeapp.permission

import androidx.annotation.StringRes
import com.example.composeapp.R
import com.example.composeapp.permission.permissionstate.PermissionState

object PermissionText {

    @StringRes
    fun title(type: PermissionType): Int =
        when (type) {
            PermissionType.BLE -> R.string.permission_ble_title
            PermissionType.CAMERA -> R.string.permission_camera_title
            PermissionType.LOCATION -> R.string.permission_location_title
            PermissionType.NOTIFICATIONS -> R.string.permission_notification_title
        }

    @StringRes
    fun message(state: PermissionState): Int =
        when (state) {
            PermissionState.Requested ->
                R.string.permission_not_requested

            PermissionState.Denied ->
                R.string.permission_denied

            PermissionState.PermanentlyDenied ->
                R.string.permission_permanently_denied

            PermissionState.Granted ->
                R.string.permission_granted
        }

    @StringRes
    fun action(): Int = R.string.permission_allow
}
