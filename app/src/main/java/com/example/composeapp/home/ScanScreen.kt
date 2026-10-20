package com.example.composeapp.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.composeapp.R
import com.example.composeapp.bledeviceui.events.ScanEvent
import com.example.composeapp.bledeviceui.uistate.ConnectionState
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import com.example.composeapp.ui.theme.HeartSpace.Dp_10
import com.example.composeapp.ui.theme.HeartSpace.Dp_12
import com.example.composeapp.ui.theme.HeartSpace.Dp_20
import com.example.composeapp.ui.theme.HeartSpace.Dp_24
import com.example.composeapp.ui.theme.HeartSpace.Dp_32
import com.example.composeapp.ui.theme.HeartSpace.Dp_48
import com.example.composeapp.ui.theme.HeartSpace.Dp_6
import com.example.composeapp.ui.theme.HeartSpace.Dp_8
import kotlinx.coroutines.flow.StateFlow

/**
 * Always-spinning indicator driven by an explicit infinite transition so it
 * keeps rotating even when the parent recomposes rapidly due to scan updates.
 */
@Composable
fun SpinningIndicator(
    modifier: Modifier = Modifier,
    size: Dp = Dp_48,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
) {
    val transition = rememberInfiniteTransition(label = "scan_spin")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "scan_rotation"
    )
    CircularProgressIndicator(
        modifier = modifier
            .size(size)
            .rotate(rotation),
        strokeWidth = strokeWidth
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    uiState: StateFlow<ScanUiState>,
    onEvent: (ScanEvent) -> Unit,
) {
    val state by uiState.collectAsState()
    val scanState = state.scanState
    val connectionState = state.connectionState
    val liveDataState = state.liveDataState

    if (scanState.showDeviceDialog) {
        DevicePickerDialog(
            devices = scanState.filteredDevices,
            isScanning = scanState.isScanning,
            profiles = scanState.profiles,
            onDeviceClick = { onEvent(ScanEvent.DeviceClicked(it)) },
            onDismiss = { onEvent(ScanEvent.DismissDialog) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFADD8E6),
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                ),
                actions = {
                    when {
                        scanState.isScanning -> {
                            IconButton(onClick = { onEvent(ScanEvent.StopScan) }) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = stringResource(R.string.cd_stop_scan),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        (connectionState is ConnectionState.Connected) || (connectionState is ConnectionState.Reconnecting) -> {
                            IconButton(onClick = { onEvent(ScanEvent.Rescan) }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.cd_rescan)
                                )
                            }
                        }
                        else -> {
                            IconButton(onClick = { onEvent(ScanEvent.StartScan) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
                                    contentDescription = stringResource(R.string.cd_scan)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dp_24),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val showSpinner = (scanState.isScanning && scanState.filteredDevices.isEmpty()) ||
                    connectionState is ConnectionState.Connecting ||
                    connectionState is ConnectionState.Reconnecting

            if (showSpinner) {
                SpinningIndicator()
                Spacer(modifier = Modifier.height(Dp_20))
            }

            when (connectionState) {
                is ConnectionState.Disconnected -> {
                    val label = when {
                        scanState.isScanning -> stringResource(R.string.scanning_for_devices)
                        scanState.filteredDevices.isNotEmpty() -> stringResource(R.string.state_select_device)
                        else -> stringResource(R.string.no_matching_devices)
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Dp_32)
                    )
                }

                is ConnectionState.Connecting -> {
                    Text(
                        text = stringResource(R.string.state_connecting, connectionState.device.name, connectionState.attempt),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Dp_32)
                    )
                }

                is ConnectionState.Connected -> {
                    Text(
                        text = stringResource(R.string.state_connected, connectionState.device.name),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Dp_32)
                    )
                    Spacer(modifier = Modifier.height(Dp_8))
                    Text(
                        text = stringResource(R.string.device_mac_rssi, connectionState.device.id, connectionState.device.rssi),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dp_20))
                    LiveDataSection(
                        selectedTab = liveDataState.selectedTab,
                        onTabSelected = { onEvent(ScanEvent.TabSelected(it)) },
                        heartRateBpm = liveDataState.heartRateBpm,
                        bodySensorLocation = liveDataState.bodySensorLocation,
                        heartRateHistory = liveDataState.heartRateHistory,
                        temperatureCelsius = liveDataState.temperatureCelsius,
                        temperatureHistory = liveDataState.temperatureHistory
                    )
                }

                is ConnectionState.Reconnecting -> {
                    Text(
                        text = stringResource(R.string.connection_lost_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dp_6))
                    Text(
                        text = stringResource(R.string.state_reconnecting, connectionState.device.name, connectionState.attempt),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Dp_32)
                    )
                    Spacer(modifier = Modifier.height(Dp_10))
                    Text(
                        text = stringResource(R.string.reconnect_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Dp_32)
                    )
                }

                is ConnectionState.Error -> {
                    Text(
                        text = connectionState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Dp_32)
                    )
                }
            }

            scanState.scanError?.let { error ->
                Spacer(modifier = Modifier.height(Dp_12))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Dp_32)
                )
            }
        }
    }
}
