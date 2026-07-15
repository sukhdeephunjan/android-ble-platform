package com.example.composeapp.permission.flow

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.composeapp.permission.PermissionEvent
import com.example.composeapp.permission.PermissionMapper
import com.example.composeapp.permission.PermissionRequest
import com.example.composeapp.permission.ui.PermissionScreen
import com.example.composeapp.permission.viewmodel.PermissionFlowViewModel
import com.example.composeapp.permission.viewmodel.PermissionViewModel

@Composable
fun PermissionFlowHost(
    permissionViewModel: PermissionViewModel,
    permissionFlowViewModel: PermissionFlowViewModel,
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val currentPermissionType by permissionFlowViewModel.currentPermission.collectAsState()
    val permissionState by permissionViewModel.permissionState.collectAsState()

    // Sync current permission type into PermissionViewModel when it changes
    LaunchedEffect(currentPermissionType) {
        currentPermissionType?.let { permissionViewModel.setPermissionType(it) }
    }

    // Listen for all-granted event
    LaunchedEffect(Unit) {
        permissionFlowViewModel.allPermissionsGranted.collect {
            onAllPermissionsGranted()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val mapped = PermissionMapper.map(context, result)
        currentPermissionType?.let { type ->
            if (mapped.granted) {
                permissionViewModel.onEvent(PermissionEvent.PermissionGranted)
                permissionFlowViewModel.onPermissionGranted(type)
            } else if (mapped.permanentlyDenied) {
                permissionViewModel.onEvent(PermissionEvent.PermissionPermanentlyDenied)
            } else {
                permissionViewModel.onEvent(PermissionEvent.PermissionDenied)
            }
        }
    }

    currentPermissionType?.let { type ->
        PermissionScreen(
            permissionType = type,
            permissionState = permissionState,
            onRequestPermission = {
                permissionLauncher.launch(PermissionRequest.permissionsFor(type))
            },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
}


