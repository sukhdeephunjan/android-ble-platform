package com.example.composeapp.bledeviceui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blelib.bleCore.BleProfile
import com.example.blelib.bleCore.BleProfileProvider
import com.example.blelib.bleCore.HeartRateData
import com.example.blelib.bleCore.TemperatureData
import com.example.composeapp.BleConnector
import com.example.composeapp.R
import com.example.composeapp.bledeviceui.events.ScanEvent
import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.bledeviceui.scanner.BleScanner
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

private const val CONNECTION_TIMEOUT_MS = 10_000L
private const val RETRY_DELAY_MS = 1_000L
private const val SCAN_TIMEOUT_MS = 30_000L

@HiltViewModel
class ScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileProvider: BleProfileProvider,
    private val bleScanner: BleScanner,
    private val bleConnector: BleConnector
) : ViewModel() {

    private val profiles: List<BleProfile> = profileProvider.provideProfiles()

    /** Service UUIDs from active profiles — passed to the OS-level scan filter. */
    private val allowedServiceUuids: List<String> = profiles
        .flatMap { it.services }
        .filter { it.required }
        .map { it.uuid.uppercase() }

    private val _uiState = MutableStateFlow(ScanUiState(profiles = profiles))
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    /** Holds the running connection-retry coroutine so it can be cancelled on Rescan/Dismiss. */
    private var connectionJob: Job? = null

    /** Cancels scan after 30 s if no matching device found. */
    private var scanTimeoutJob: Job? = null

    init {
        // Auto-start scan when user lands on screen
        startScanning()

        // Collect live scan results. As soon as a device advertising one of the
        // required service UUIDs is discovered, stop scanning and open the dialog.
        viewModelScope.launch {
            bleScanner.scanResults.collect { devices ->
                val filtered = applyProfileFilter(devices, _uiState.value.selectedProfileId)
                val hasTargetDevice = filtered.any { matchesRequiredService(it) }

                if (hasTargetDevice && _uiState.value.isScanning) {
                    // Target device found → stop scanning immediately
                    scanTimeoutJob?.cancel()
                    bleScanner.stopScan()
                    _uiState.update { state ->
                        state.copy(
                            allDevices = devices,
                            filteredDevices = filtered,
                            isScanning = false,
                            showDeviceDialog = true,
                            connectionStateLabel = context.getString(R.string.state_select_device)
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            allDevices = devices,
                            filteredDevices = filtered,
                            showDeviceDialog = state.isScanning && filtered.isNotEmpty()
                        )
                    }
                }
            }
        }

        // Forward scanner errors to UI state
        viewModelScope.launch {
            bleScanner.scanError.collect { error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            errorMessage = error,
                            connectionStateLabel = context.getString(R.string.state_scan_error, error)
                        )
                    }
                }
            }
        }

        // Collect parsed values notified by the connected peripheral
        viewModelScope.launch {
            bleConnector.dataUpdates.collect { data ->
                when (val value = data.value) {
                    is HeartRateData -> _uiState.update { it.copy(heartRateBpm = value.bpm) }
                    is TemperatureData -> _uiState.update { it.copy(temperatureCelsius = value.celsius) }
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

            is ScanEvent.DeviceClicked -> connectToDevice(event.device)

            ScanEvent.StartScan -> startScanning()

            ScanEvent.StopScan -> {
                scanTimeoutJob?.cancel()
                bleScanner.stopScan()
                _uiState.update { state ->
                    state.copy(
                        isScanning = false,
                        showDeviceDialog = state.filteredDevices.isNotEmpty(),
                        connectionStateLabel = if (state.filteredDevices.isNotEmpty())
                            context.getString(R.string.state_select_device)
                        else
                            context.getString(R.string.no_matching_devices)
                    )
                }
            }

            ScanEvent.DismissDialog -> {
                scanTimeoutJob?.cancel()
                bleScanner.stopScan()
                connectionJob?.cancel()
                _uiState.update { state ->
                    state.copy(
                        isScanning = false,
                        showDeviceDialog = false,
                        connectionStateLabel = if (state.connectedDevice != null)
                            context.getString(R.string.state_connected_label, state.connectedDevice.name)
                        else
                            context.getString(R.string.state_idle)
                    )
                }
            }

            ScanEvent.Rescan -> {
                scanTimeoutJob?.cancel()
                connectionJob?.cancel()
                bleConnector.disconnect()
                startScanning()
                _uiState.update {
                    it.copy(
                        allDevices = emptyList(),
                        filteredDevices = emptyList(),
                        connectedDevice = null,
                        errorMessage = null,
                        showDeviceDialog = false,
                        connectionAttempt = 0,
                        heartRateBpm = null,
                        temperatureCelsius = null,
                        connectionStateLabel = context.getString(R.string.scanning_for_devices)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanTimeoutJob?.cancel()
        connectionJob?.cancel()
        bleScanner.stopScan()
        bleConnector.disconnect()
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * True when the device advertises at least one of the required service UUIDs
     * (Heart Rate / Temperature). Used to decide when to stop the scan early.
     */
    private fun matchesRequiredService(device: UiBleDevice): Boolean =
        device.serviceUuids.any { it.uppercase() in allowedServiceUuids }

    private fun startScanning() {
        scanTimeoutJob?.cancel()
        _uiState.update {
            it.copy(
                isScanning = true,
                errorMessage = null,
                showDeviceDialog = false,
                connectionStateLabel = context.getString(R.string.scanning_for_devices)
            )
        }
        bleScanner.startScan(allowedServiceUuids)

        // Auto-stop after 30 s if no matching device was found
        scanTimeoutJob = viewModelScope.launch {
            delay(SCAN_TIMEOUT_MS)
            // Only act if still scanning and no matching devices appeared
            if (_uiState.value.isScanning && _uiState.value.filteredDevices.isEmpty()) {
                bleScanner.stopScan()
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        connectionStateLabel = context.getString(R.string.no_matching_devices)
                    )
                }
            }
        }
    }

    /**
     * Initiates a connection to [device] with a 10-second timeout per attempt.
     * Retries automatically until connected or the job is cancelled (Rescan / Dismiss).
     */
    private fun connectToDevice(device: UiBleDevice) {
        scanTimeoutJob?.cancel()
        bleScanner.stopScan()
        connectionJob?.cancel()

        connectionJob = viewModelScope.launch {
            var attempt = 1
            while (isActive) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        showDeviceDialog = false,
                        connectionAttempt = attempt,
                        heartRateBpm = null,
                        temperatureCelsius = null,
                        connectionStateLabel = context.getString(
                            R.string.state_connecting, device.name, attempt
                        )
                    )
                }

                // Try to connect; null means timed-out
                val result = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
                    bleConnector.connect(device.id)
                }

                when {
                    result == null -> {
                        // Timed out — disconnect GATT cleanly and retry
                        bleConnector.disconnect()
                        attempt++
                        delay(RETRY_DELAY_MS)
                    }
                    result.isSuccess -> {
                        _uiState.update {
                            it.copy(
                                connectedDevice = device,
                                connectionStateLabel = context.getString(
                                    R.string.state_connected, device.name
                                )
                            )
                        }
                        break
                    }
                    else -> {
                        // Explicit failure (GATT error) — retry
                        attempt++
                        delay(RETRY_DELAY_MS)
                    }
                }
            }
        }
    }

    /**
     * Filters devices by the selected profile chip.
     *
     * "All" (null) → returns all devices that arrived from the scanner.
     *   No secondary UUID check — the hardware ScanFilter already ensures only
     *   Heart Rate / Temperature devices reach this point. iOS peripherals may
     *   deliver the advertising packet without service UUIDs in ScanRecord even
     *   when the hardware filter matched; a UUID check here would falsely reject them.
     *
     * Profile chip selected → keeps only devices advertising ALL required service
     *   UUIDs for that profile.
     */
    private fun applyProfileFilter(
        devices: List<UiBleDevice>,
        profileId: String?
    ): List<UiBleDevice> {
        return if (profileId == null) {
            // All devices that passed the hardware filter are valid
            devices
        } else {
            val profile = profiles.find { it.id == profileId } ?: return devices
            val requiredUuids = profile.services
                .filter { it.required }
                .map { it.uuid.uppercase() }
                .toSet()
            devices.filter { device ->
                requiredUuids.all { uuid ->
                    device.serviceUuids.any { it.uppercase() == uuid }
                }
            }
        }
    }
}
