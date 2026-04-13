package com.example.composeapp.permission.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.composeapp.permission.PermissionEvent
import com.example.composeapp.permission.PermissionMapper
import com.example.composeapp.permission.PermissionRequest
import com.example.composeapp.permission.PermissionType
import com.example.composeapp.permission.viewmodel.PermissionViewModel
import com.example.composeapp.permission.permissionstate.PermissionState

@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel = hiltViewModel(),
    onGranted: () -> Unit,
    permissionType: PermissionType
) {
    val state = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    LaunchedEffect(permissionType) {
        viewModel.setPermission(permissionType)
    }

    // Auto-check when returning from Settings or starting
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (PermissionChecker.isGranted(context, state.permissionType)) {
                    viewModel.onEvent(PermissionEvent.PermissionGranted)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val mapped = PermissionMapper.map(context, result)
            when {
                mapped.granted ->
                    viewModel.onEvent(PermissionEvent.PermissionGranted)

                mapped.permanentlyDenied ->
                    viewModel.onEvent(PermissionEvent.PermissionPermanentlyDenied)

                else ->
                    viewModel.onEvent(PermissionEvent.PermissionDenied)
            }
        }

    LaunchedEffect(state.state) {
        if (state.state == PermissionState.Granted) {
            onGranted()
        }
    }

    PermissionContent(
        permissionType = state.permissionType,
        state = state.state,
        onRequest = {
            when (state.state) {
                PermissionState.PermanentlyDenied -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
                else -> {
                    permissionLauncher.launch(
                        PermissionRequest.permissionsFor(state.permissionType)
                    )
                }
            }
        }
    )
}
