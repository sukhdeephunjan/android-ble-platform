package com.example.composeapp.permission.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.composeapp.permission.ui.PermissionScreen
import com.example.composeapp.permission.viewmodel.PermissionFlowViewModel
import com.example.composeapp.permission.viewmodel.PermissionViewModel

@Composable
fun PermissionFlowHost(
    permissionViewModel: PermissionViewModel,
    permissionFlowViewModel: PermissionFlowViewModel,
    onAllPermissionsGranted: () -> Unit
) {
    val flowState by permissionFlowViewModel.state.collectAsState()

    // 1. Trigger navigation whenever isComplete is true
    LaunchedEffect(flowState.isComplete) {
        if (flowState.isComplete) {
            onAllPermissionsGranted()
        }
    }

    // 2. Start flow ONCE
    LaunchedEffect(Unit) {
        permissionFlowViewModel.start(permissionViewModel)
    }

    // 3. UI logic
    if (!flowState.isComplete && flowState.currentPermission != null) {
        PermissionScreen(
            viewModel = permissionViewModel,
            onGranted = {
                permissionFlowViewModel.onPermissionResolved(permissionViewModel)
            },
            permissionType = flowState.currentPermission!!
        )
    }
}
