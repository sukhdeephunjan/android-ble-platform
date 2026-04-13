package com.example.composeapp.permission.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.composeapp.permission.PermissionType
import com.example.composeapp.permission.flow.PermissionFlowState
import com.example.composeapp.permission.flow.PermissionStep
import com.example.composeapp.permission.ui.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PermissionFlowViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // Only BLE permission is required
    private val steps = listOf(
        PermissionStep(PermissionType.BLE, mandatory = true)
    )
    
    private val _state = MutableStateFlow(PermissionFlowState())
    val state: StateFlow<PermissionFlowState> = _state.asStateFlow()

    fun start(permissionViewModel: PermissionViewModel) {
        processNextStep(permissionViewModel)
    }

    fun onPermissionResolved(permissionViewModel: PermissionViewModel) {
        processNextStep(permissionViewModel)
    }

    private fun processNextStep(permissionViewModel: PermissionViewModel) {
        // Find the first step that hasn't been granted yet
        val nextStep = steps.firstOrNull { step ->
            !PermissionChecker.isGranted(context, step.type)
        }

        if (nextStep == null) {
            // All mandatory permissions are granted
            _state.value = PermissionFlowState(isComplete = true)
        } else {
            permissionViewModel.setPermission(nextStep.type)
            _state.value = PermissionFlowState(currentPermission = nextStep.type)
        }
    }
}
