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
            
            PermissionState.Rationale ->
                R.string.permission_rationale

            PermissionState.PermanentlyDenied ->
                R.string.permission_permanently_denied

            PermissionState.Granted ->
                R.string.permission_granted
        }

    @StringRes
    fun action(state: PermissionState): Int = 
        when (state) {
            PermissionState.PermanentlyDenied -> R.string.permission_settings
            PermissionState.Rationale -> R.string.permission_retry
            else -> R.string.permission_allow
        }
}
