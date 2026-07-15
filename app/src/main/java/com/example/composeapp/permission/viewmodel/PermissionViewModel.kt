package com.example.composeapp.permission.viewmodel

import androidx.lifecycle.ViewModel
import com.example.composeapp.permission.PermissionEvent
import com.example.composeapp.permission.PermissionType
import com.example.composeapp.permission.permissionstate.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor() : ViewModel() {

    private val _permissionType = MutableStateFlow(PermissionType.BLE)
    val permissionType: StateFlow<PermissionType> = _permissionType.asStateFlow()

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Requested)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    fun setPermissionType(type: PermissionType) {
        _permissionType.value = type
        _permissionState.value = PermissionState.Requested
    }

    fun onEvent(event: PermissionEvent) {
        _permissionState.value = when (event) {
            PermissionEvent.PermissionGranted -> PermissionState.Granted
            PermissionEvent.PermissionDenied -> PermissionState.Denied
            PermissionEvent.PermissionPermanentlyDenied -> PermissionState.PermanentlyDenied
            PermissionEvent.PermissionRequest -> PermissionState.Rationale
        }
    }
}

