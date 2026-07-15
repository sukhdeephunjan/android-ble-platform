package com.example.composeapp.permission.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composeapp.permission.PermissionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionFlowViewModel @Inject constructor() : ViewModel() {

    // BLE apps require BLE + Location permissions
    private val requiredPermissions = listOf(PermissionType.BLE, PermissionType.LOCATION)
    private val grantedPermissions = mutableSetOf<PermissionType>()

    private val _currentPermission = MutableStateFlow<PermissionType?>(requiredPermissions.first())
    val currentPermission: StateFlow<PermissionType?> = _currentPermission.asStateFlow()

    private val _allPermissionsGranted = MutableSharedFlow<Unit>(replay = 0)
    val allPermissionsGranted = _allPermissionsGranted.asSharedFlow()

    fun onPermissionGranted(type: PermissionType) {
        grantedPermissions.add(type)
        val next = requiredPermissions.firstOrNull { it !in grantedPermissions }
        _currentPermission.value = next
        if (next == null) {
            viewModelScope.launch { _allPermissionsGranted.emit(Unit) }
        }
    }

    fun onPermissionSkipped(type: PermissionType) {
        // Move to next even if not granted (e.g. Location on older SDK is bundled with BLE)
        val next = requiredPermissions.firstOrNull { it !in grantedPermissions && it != type }
        _currentPermission.value = next
        if (next == null) {
            viewModelScope.launch { _allPermissionsGranted.emit(Unit) }
        }
    }
}

