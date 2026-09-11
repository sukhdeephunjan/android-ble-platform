package com.example.composeapp.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composeapp.R
import com.example.composeapp.bledeviceui.events.ScanEvent
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Always-spinning indicator driven by an explicit infinite transition so it
 * keeps rotating even when the parent recomposes rapidly due to scan updates.
 */
@Composable
fun SpinningIndicator(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = ProgressIndicatorDefaults.CircularStrokeWidth
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

/**
 * Shows the latest heart-rate reading with a pulsing heart icon.
 * While no notification has arrived yet a "waiting" hint is displayed.
 */
@Composable
fun HeartRateCard(bpm: Int?) {
    val transition = rememberInfiniteTransition(label = "hr_pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hr_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.heart_rate_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (bpm == null) {
                Text(
                    text = stringResource(R.string.heart_rate_waiting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.cd_heart_rate),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(36.dp)
                            .scale(scale)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.heart_rate_bpm, bpm),
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.heart_rate_unit),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    uiState: StateFlow<ScanUiState>,
    onEvent: (ScanEvent) -> Unit
) {
    val state by uiState.collectAsState()

    if (state.showDeviceDialog) {
        DevicePickerDialog(
            devices = state.filteredDevices,
            isScanning = state.isScanning,
            profiles = state.profiles,
            onDeviceClick = { onEvent(ScanEvent.DeviceClicked(it)) },
            onDismiss = { onEvent(ScanEvent.DismissDialog) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFADD8E6),          // light blue background
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                ),
                actions = {
                    when {
                        state.isScanning -> {
                            IconButton(onClick = { onEvent(ScanEvent.StopScan) }) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = stringResource(R.string.cd_stop_scan),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        state.connectedDevice != null -> {
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
                                    imageVector = Icons.Default.BluetoothSearching,
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isScanning && state.filteredDevices.isEmpty()) {
                SpinningIndicator()
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = state.connectionStateLabel,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            state.connectedDevice?.let { device ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.device_mac_rssi, device.id, device.rssi),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
                HeartRateCard(bpm = state.heartRateBpm)

                state.temperatureCelsius?.let { celsius ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.temperature_title) + ": " +
                                stringResource(R.string.temperature_value, celsius),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}
