package com.example.composeapp.permission.ui

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.composeapp.permission.PermissionEvent
import com.example.composeapp.permission.PermissionMapper
import com.example.composeapp.permission.PermissionRequest
import com.example.composeapp.permission.PermissionType
import com.example.composeapp.permission.viewmodel.PermissionViewModel
import com.example.composeapp.permission.permissionstate.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel = hiltViewModel(),
    onGranted: () -> Unit, permissionType: PermissionType
) {
    val state = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    
    LaunchedEffect(permissionType) {
        viewModel.setPermission(permissionType)
    }

    LaunchedEffect(Unit) {
        if (PermissionChecker.isGranted(context, state.permissionType)) {
            viewModel.onEvent(PermissionEvent.PermissionGranted)
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
            permissionLauncher.launch(
                PermissionRequest.permissionsFor(state.permissionType)
            )
        }
    )
}

private fun isPermanentlyDenied(
    context: Context,
    result: Map<String, Boolean>
): Boolean {
    val activity = context as? Activity ?: return false

    return result.any { (permission, granted) ->
        !granted && !ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            permission
        )
    }
}

