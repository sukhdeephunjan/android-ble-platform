package com.example.composeapp.permission.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.composeapp.permission.ui.PermissionScreen
import com.example.composeapp.permission.viewmodel.PermissionFlowViewModel
import com.example.composeapp.permission.viewmodel.PermissionViewModel

@Composable
fun PermissionFlowHost(
    permissionViewModel: PermissionViewModel,
    permissionFlowViewModel: PermissionFlowViewModel,
    onAllPermissionsGranted: () -> Unit
) {
    val flowState = permissionFlowViewModel.state.collectAsState().value

    // Start permission flow ONCE
    LaunchedEffect(Unit) {
        permissionFlowViewModel.start(permissionViewModel)
    }

    when {
        flowState.isComplete -> {
            LaunchedEffect(true) {
                    onAllPermissionsGranted()
            }
        }

        flowState.currentPermission != null -> {
            PermissionScreen(
                viewModel = permissionViewModel,
                onGranted = {
                    permissionFlowViewModel.onPermissionResolved(permissionViewModel)
                }, flowState.currentPermission
            )
        }
    }
}
