package com.example.composeapp.bledeviceui.uistate

import com.example.blelib.bleCore.BleProfile
import com.example.blelib.bleCore.BodySensorLocation
import com.example.composeapp.bledeviceui.model.UiBleDevice

/**
 * State of the BLE Scanner domain (device discovery, profile filtering, dialog visibility).
 */
data class ScanState(
    val isScanning: Boolean = false,
    val allDevices: List<UiBleDevice> = emptyList(),
    val filteredDevices: List<UiBleDevice> = emptyList(),
    val selectedProfileId: String? = null,
    val profiles: List<BleProfile> = emptyList(),
    val showDeviceDialog: Boolean = false,
    val scanError: String? = null,
)

/**
 * Sealed interface representing valid GATT connection lifecycle states.
 */
sealed interface ConnectionState {
    data object Disconnected : ConnectionState

    data class Connecting(
        val device: UiBleDevice,
        val attempt: Int,
    ) : ConnectionState

    data class Connected(
        val device: UiBleDevice,
    ) : ConnectionState

    data class Reconnecting(
        val device: UiBleDevice,
        val attempt: Int,
    ) : ConnectionState

    data class Error(
        val message: String,
    ) : ConnectionState
}

/**
 * State of live GATT telemetry data and chart histories.
 */
data class LiveDataState(
    val heartRateBpm: Int? = null,
    val bodySensorLocation: BodySensorLocation? = null,
    val temperatureCelsius: Float? = null,
    /** Heart-rate samples taken once per second, oldest → newest. */
    val heartRateHistory: List<Int> = emptyList(),
    /** Temperature samples taken once per second, oldest → newest. */
    val temperatureHistory: List<Float> = emptyList(),
    /** Currently visible chart tab. */
    val selectedTab: LiveDataTab = LiveDataTab.HEART_RATE,
)

/** Tabs of the live-data section. Heart rate is the default. */
enum class LiveDataTab {
    HEART_RATE,
    TEMPERATURE,
}

/**
 * Screen-level UI state wrapper combining the 3 segregated domain states.
 */
data class ScanUiState(
    val scanState: ScanState = ScanState(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val liveDataState: LiveDataState = LiveDataState(),
)
