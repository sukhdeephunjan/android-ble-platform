package com.example.composeapp.bledeviceui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blelib.bleCore.BleProfile
import com.example.blelib.bleCore.BleProfileProvider
import com.example.composeapp.bledeviceui.events.ScanEvent
import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.bledeviceui.scanner.BleScanner
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val profileProvider: BleProfileProvider,
    private val bleScanner: BleScanner
) : ViewModel() {

    private val profiles: List<BleProfile> = profileProvider.provideProfiles()

    private val _uiState = MutableStateFlow(ScanUiState(profiles = profiles))
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        // Collect live scan results from BleScanner
        viewModelScope.launch {
            bleScanner.scanResults.collect { devices ->
                _uiState.update { state ->
                    state.copy(
                        allDevices = devices,
                        filteredDevices = applyProfileFilter(devices, state.selectedProfileId)
                    )
                }
            }
        }
        // Forward scanner errors to UI state
        viewModelScope.launch {
            bleScanner.scanError.collect { error ->
                if (error != null) {
                    _uiState.update { it.copy(isScanning = false, errorMessage = error) }
                }
            }
        }
    }

    fun onEvent(event: ScanEvent) {
        when (event) {
            is ScanEvent.ProfileSelected -> {
                _uiState.update { state ->
                    state.copy(
                        selectedProfileId = event.profileId,
                        filteredDevices = applyProfileFilter(state.allDevices, event.profileId)
                    )
                }
            }
            is ScanEvent.DeviceClicked -> {
                // TODO: navigate to device detail / trigger connection
            }
            ScanEvent.StartScan -> {
                _uiState.update { it.copy(isScanning = true, errorMessage = null) }
                bleScanner.startScan()
            }
            ScanEvent.StopScan -> {
                bleScanner.stopScan()
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleScanner.stopScan()
    }

    /**
     * If no profile is selected (null) — return all devices.
     * Otherwise keep only devices that advertise ALL required services of that profile.
     */
    private fun applyProfileFilter(
        devices: List<UiBleDevice>,
        profileId: String?
    ): List<UiBleDevice> {
        if (profileId == null) return devices
        val profile = profiles.find { it.id == profileId } ?: return devices
        val requiredUuids = profile.services
            .filter { it.required }
            .map { it.uuid.uppercase() }
            .toSet()
        return devices.filter { device ->
            requiredUuids.all { uuid ->
                device.serviceUuids.any { it.uppercase() == uuid }
            }
        }
    }
}
