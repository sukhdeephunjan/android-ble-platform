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
    private var currentIndex = 0
    // 🔹 Configure your permission order here
    private val steps = listOf(
        PermissionStep(PermissionType.BLE, mandatory = true),
        PermissionStep(PermissionType.NOTIFICATIONS, mandatory = false)
    )
    private val _state = MutableStateFlow(PermissionFlowState())
    val state: StateFlow<PermissionFlowState> = _state.asStateFlow()

    // ----------------------------------
    // ENTRY POINT
    // ----------------------------------
    fun start(permissionViewModel: PermissionViewModel) {
        moveToNextPermission(permissionViewModel)
    }

    fun onPermissionResolved(permissionViewModel: PermissionViewModel) {
        moveToNextPermission(permissionViewModel)
    }
    private fun moveToNextPermission(permissionViewModel: PermissionViewModel) {
        val next = steps.firstOrNull { step ->
            !PermissionChecker.isGranted(
                context,
                step.type
            )
        }

        if (next == null) {
            _state.value = PermissionFlowState(isComplete = true)
        } else {
            permissionViewModel.setPermission(next.type)
            _state.value = PermissionFlowState(currentPermission = next.type)
        }
    }
}
