package com.example.composeapp.bledeviceui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blelib.bleCore.BleProfile
import com.example.blelib.bleCore.BleProfileProvider
import com.example.blelib.bleCore.BodySensorLocation
import com.example.blelib.bleCore.HeartRateData
import com.example.blelib.bleCore.TemperatureData
import com.example.blelib.bleCore.Uuids
import com.example.composeapp.BleConnector
import com.example.composeapp.bledeviceui.events.ScanEvent
import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.bledeviceui.scanner.BleScanner
import com.example.composeapp.bledeviceui.uistate.ConnectionState
import com.example.composeapp.bledeviceui.uistate.LiveDataState
import com.example.composeapp.bledeviceui.uistate.ScanState
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlin.random.Random

private const val CONNECTION_TIMEOUT_MS = 10_000L
private const val RETRY_DELAY_MS = 1_000L
private const val SCAN_TIMEOUT_MS = 30_000L

private const val RECONNECT_INITIAL_DELAY_MS = 1_000L
private const val RECONNECT_MAX_DELAY_MS = 30_000L
private const val RECONNECT_JITTER_MS = 500L
private const val RECONNECT_TIMEOUT_MS = 30_000L

private const val SAMPLE_INTERVAL_MS = 1_000L
private const val MAX_HISTORY_POINTS = 120

@HiltViewModel
class ScanViewModel @Inject constructor(
    profileProvider: BleProfileProvider,
    private val bleScanner: BleScanner,
    private val bleConnector: BleConnector,
) : ViewModel() {

    private val profiles: List<BleProfile> = profileProvider.provideProfiles()

    private val allowedServiceUuids: List<String> = profiles
        .asSequence()
        .flatMap { it.services }
        .filter { it.required }
        .map { it.uuid.uppercase() }
        .toList()

    private val heartRateServiceUuid: String = Uuids.HEART_RATE_SERVICE.uppercase()

    private val _uiState = MutableStateFlow(
        ScanUiState(scanState = ScanState(profiles = profiles))
    )
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var connectionJob: Job? = null
    private var scanTimeoutJob: Job? = null
    private var reconnectJob: Job? = null
    private var samplingJob: Job? = null

    init {
        startScanning()

        viewModelScope.launch {
            bleScanner.scanResults.collect { devices ->
                val selectedProfileId = _uiState.value.scanState.selectedProfileId
                val filtered = applyProfileFilter(devices, selectedProfileId)
                val hasTargetDevice = filtered.any { hasHeartRateService(it) }

                if (hasTargetDevice && _uiState.value.scanState.isScanning) {
                    scanTimeoutJob?.cancel()
                    bleScanner.stopScan()
                    _uiState.update { state ->
                        state.copy(
                            scanState = state.scanState.copy(
                                allDevices = devices,
                                filteredDevices = filtered,
                                isScanning = false,
                                showDeviceDialog = true
                            )
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            scanState = state.scanState.copy(
                                allDevices = devices,
                                filteredDevices = filtered,
                                showDeviceDialog = state.scanState.isScanning && filtered.isNotEmpty()
                            )
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            bleScanner.scanError.collect { error ->
                if (error != null) {
                    _uiState.update { state ->
                        state.copy(
                            scanState = state.scanState.copy(
                                isScanning = false,
                                scanError = error
                            )
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            bleConnector.dataUpdates.collect { data ->
                when (val value = data.value) {
                    is HeartRateData -> _uiState.update { state ->
                        state.copy(liveDataState = state.liveDataState.copy(heartRateBpm = value.bpm))
                    }
                    is BodySensorLocation -> _uiState.update { state ->
                        state.copy(liveDataState = state.liveDataState.copy(bodySensorLocation = value))
                    }
                    is TemperatureData -> _uiState.update { state ->
                        state.copy(liveDataState = state.liveDataState.copy(temperatureCelsius = value.celsius))
                    }
                }
            }
        }

        viewModelScope.launch {
            bleConnector.disconnections.collect { startReconnecting() }
        }
    }

    fun onEvent(event: ScanEvent) {
        when (event) {
            is ScanEvent.ProfileSelected -> {
                _uiState.update { state ->
                    val filtered = applyProfileFilter(state.scanState.allDevices, event.profileId)
                    state.copy(
                        scanState = state.scanState.copy(
                            selectedProfileId = event.profileId,
                            filteredDevices = filtered
                        )
                    )
                }
            }

            is ScanEvent.DeviceClicked -> connectToDevice(event.device)

            is ScanEvent.TabSelected -> {
                _uiState.update { state ->
                    state.copy(liveDataState = state.liveDataState.copy(selectedTab = event.tab))
                }
            }

            ScanEvent.StartScan -> startScanning()

            ScanEvent.StopScan -> {
                scanTimeoutJob?.cancel()
                bleScanner.stopScan()
                _uiState.update { state ->
                    state.copy(
                        scanState = state.scanState.copy(
                            isScanning = false,
                            showDeviceDialog = state.scanState.filteredDevices.isNotEmpty()
                        )
                    )
                }
            }

            ScanEvent.DismissDialog -> {
                scanTimeoutJob?.cancel()
                bleScanner.stopScan()
                connectionJob?.cancel()
                _uiState.update { state ->
                    state.copy(
                        scanState = state.scanState.copy(
                            isScanning = false,
                            showDeviceDialog = false
                        )
                    )
                }
            }

            ScanEvent.Rescan -> {
                scanTimeoutJob?.cancel()
                connectionJob?.cancel()
                reconnectJob?.cancel()
                samplingJob?.cancel()
                bleConnector.disconnect()
                startScanning()
                _uiState.update {
                    ScanUiState(
                        scanState = ScanState(
                            isScanning = true,
                            profiles = profiles
                        ),
                        connectionState = ConnectionState.Disconnected,
                        liveDataState = LiveDataState()
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanTimeoutJob?.cancel()
        connectionJob?.cancel()
        reconnectJob?.cancel()
        samplingJob?.cancel()
        bleScanner.stopScan()
        bleConnector.disconnect()
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun hasHeartRateService(device: UiBleDevice): Boolean =
        device.serviceUuids.any { it.uppercase() == heartRateServiceUuid }

    private fun startScanning() {
        scanTimeoutJob?.cancel()
        reconnectJob?.cancel()
        _uiState.update { state ->
            state.copy(
                scanState = state.scanState.copy(
                    isScanning = true,
                    scanError = null,
                    showDeviceDialog = false
                )
            )
        }
        bleScanner.startScan(allowedServiceUuids)

        scanTimeoutJob = viewModelScope.launch {
            delay(SCAN_TIMEOUT_MS)
            if (_uiState.value.scanState.isScanning && _uiState.value.scanState.filteredDevices.isEmpty()) {
                bleScanner.stopScan()
                _uiState.update { state ->
                    state.copy(
                        scanState = state.scanState.copy(isScanning = false)
                    )
                }
            }
        }
    }

    private fun connectToDevice(device: UiBleDevice) {
        scanTimeoutJob?.cancel()
        bleScanner.stopScan()
        connectionJob?.cancel()
        reconnectJob?.cancel()
        samplingJob?.cancel()

        connectionJob = viewModelScope.launch {
            delay(150)
            var attempt = 1
            while (isActive) {
                _uiState.update { state ->
                    state.copy(
                        scanState = state.scanState.copy(
                            isScanning = false,
                            showDeviceDialog = false
                        ),
                        connectionState = ConnectionState.Connecting(device, attempt),
                        liveDataState = LiveDataState(
                            selectedTab = state.liveDataState.selectedTab
                        )
                    )
                }

                val result = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
                    bleConnector.connect(device.id, autoConnect = false)
                }

                when {
                    result == null -> {
                        bleConnector.disconnect()
                        attempt++
                        delay(RETRY_DELAY_MS)
                    }
                    result.isSuccess -> {
                        _uiState.update { state ->
                            state.copy(
                                connectionState = ConnectionState.Connected(device)
                            )
                        }
                        startSampling()
                        break
                    }
                    else -> {
                        bleConnector.disconnect()
                        attempt++
                        delay(RETRY_DELAY_MS)
                    }
                }
            }
        }
    }

    private fun startSampling() {
        samplingJob?.cancel()
        samplingJob = viewModelScope.launch {
            while (isActive) {
                delay(SAMPLE_INTERVAL_MS)
                _uiState.update { state ->
                    val live = state.liveDataState
                    val bpm = live.heartRateBpm
                    val celsius = live.temperatureCelsius
                    state.copy(
                        liveDataState = live.copy(
                            heartRateHistory = if (bpm == null) live.heartRateHistory
                            else (live.heartRateHistory + bpm).takeLast(MAX_HISTORY_POINTS),
                            temperatureHistory = if (celsius == null) live.temperatureHistory
                            else (live.temperatureHistory + celsius).takeLast(MAX_HISTORY_POINTS)
                        )
                    )
                }
            }
        }
    }

    private fun startReconnecting() {
        val device = when (val conn = _uiState.value.connectionState) {
            is ConnectionState.Connected -> conn.device
            is ConnectionState.Connecting -> conn.device
            is ConnectionState.Reconnecting -> conn.device
            else -> return
        }

        connectionJob?.cancel()
        reconnectJob?.cancel()
        samplingJob?.cancel()

        reconnectJob = viewModelScope.launch {
            var attempt = 1
            while (isActive) {
                _uiState.update { state ->
                    state.copy(
                        scanState = state.scanState.copy(
                            isScanning = false,
                            showDeviceDialog = false
                        ),
                        connectionState = ConnectionState.Reconnecting(device, attempt),
                        liveDataState = state.liveDataState.copy(
                            heartRateBpm = null,
                            bodySensorLocation = null,
                            temperatureCelsius = null
                        )
                    )
                }

                val result = withTimeoutOrNull(RECONNECT_TIMEOUT_MS) {
                    bleConnector.connect(device.id, autoConnect = true)
                }

                if (result?.isSuccess == true) {
                    _uiState.update { state ->
                        state.copy(
                            connectionState = ConnectionState.Connected(device)
                        )
                    }
                    startSampling()
                    return@launch
                }

                bleConnector.disconnect()
                delay(reconnectDelayFor(attempt))
                attempt++
            }
        }
    }

    private fun reconnectDelayFor(attempt: Int): Long {
        val exponential = RECONNECT_INITIAL_DELAY_MS shl (attempt - 1).coerceIn(0, 10)
        val capped = exponential.coerceAtMost(RECONNECT_MAX_DELAY_MS)
        return capped + Random.nextLong(RECONNECT_JITTER_MS)
    }

    private fun applyProfileFilter(
        devices: List<UiBleDevice>,
        profileId: String?
    ): List<UiBleDevice> {
        return if (profileId == null) {
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
