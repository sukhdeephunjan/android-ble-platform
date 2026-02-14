package com.example.composeapp.bledeviceui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blelib.bleCore.StandardBleProfiles
import com.example.composeapp.bledeviceui.scanner.ScanResultMapper
import com.example.composeapp.bledeviceui.scanner.BleScanner
import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.bledeviceui.events.ScanEvent
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val bleScanner: BleScanner,
    private val resultMapper: ScanResultMapper
) : ViewModel() {

    private val bleProfiles = StandardBleProfiles.provideProfiles()

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onEvent(event: ScanEvent) {
        when (event) {
            ScanEvent.StartScan -> startScan()
            ScanEvent.StopScan -> stopScan()
            is ScanEvent.ProfileSelected -> updateProfileFilter(event.profileId)
            is ScanEvent.DeviceClicked -> {
                // navigation later
            }
        }
    }

    private fun startScan() {
        if (_uiState.value.isScanning) return

        _uiState.update { it.copy(isScanning = true) }

        bleScanner.scan()
            .onEach { wrapper ->

                val device = resultMapper.map(wrapper, bleProfiles)
                    ?: return@onEach

                _uiState.update { state ->
                    val updatedAll = (state.allDevices + device)
                        .distinctBy { it.id }

                    state.copy(
                        allDevices = updatedAll,
                        visibleDevices = filterDevices(
                            updatedAll,
                            state.selectedProfileId
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun stopScan() {
        bleScanner.stop()
        _uiState.update { it.copy(isScanning = false) }
    }

    private fun updateProfileFilter(profileId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedProfileId = profileId,
                visibleDevices = filterDevices(state.allDevices, profileId)
            )
        }
    }

    private fun filterDevices(
        devices: List<UiBleDevice>,
        profileId: String?
    ): List<UiBleDevice> {
        if (profileId == null) return devices
        return devices.filter { profileId in it.profileIds }
    }
}
