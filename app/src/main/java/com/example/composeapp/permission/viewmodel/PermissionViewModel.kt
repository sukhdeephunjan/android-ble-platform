package com.example.composeapp.permission.viewmodel

import androidx.lifecycle.ViewModel
import com.example.composeapp.permission.PermissionError
import com.example.composeapp.permission.PermissionEvent
import com.example.composeapp.permission.PermissionType
import com.example.composeapp.permission.permissionstate.PermissionState
import com.example.composeapp.permission.permissionstate.PermissionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor() :
    ViewModel() {

    private val _uiState =
        MutableStateFlow(
            PermissionUiState(permissionType = PermissionType.BLE)
        )

    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()
    fun setPermission(type: PermissionType) {
        _uiState.value = PermissionUiState(permissionType = type)
    }

    fun onEvent(event: PermissionEvent) {
        _uiState.update { state ->
            when (event) {
                PermissionEvent.PermissionGranted ->
                    state.copy(
                        state = PermissionState.Granted,
                        error = null
                    )

                PermissionEvent.PermissionDenied ->
                    state.copy(
                        state = PermissionState.Denied,
                        error = PermissionError.Denied
                    )

                PermissionEvent.PermissionPermanentlyDenied ->
                    state.copy(
                        state = PermissionState.PermanentlyDenied,
                        error = PermissionError.PermanentlyDenied
                    )

                PermissionEvent.PermissionRequest ->
                    state
            }
        }
    }
}