package com.example.composeapp.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    uiState: StateFlow<ScanUiState>,
    onProfileSelected: (String?) -> Unit,
    onDeviceClick: (UiBleDevice) -> Unit = {},
    onStartScan: () -> Unit = {},
    onStopScan: () -> Unit = {}
) {
    val state by uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Scan Devices") })
        },
        floatingActionButton = {
            if (state.isScanning) {
                ExtendedFloatingActionButton(
                    onClick = onStopScan,
                    icon = { Icon(Icons.Default.Stop, contentDescription = "Stop") },
                    text = { Text("Stop") },
                    containerColor = MaterialTheme.colorScheme.error
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = onStartScan,
                    icon = { Icon(Icons.Default.Search, contentDescription = "Scan") },
                    text = { Text("Scan") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Profile filter chips — populated from ViewModel state
            ProfileFilterRow(
                profiles = state.profiles,
                selectedProfileId = state.selectedProfileId,
                onProfileSelected = onProfileSelected
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Error banner
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Empty state
            val displayDevices = state.filteredDevices
            if (!state.isScanning && displayDevices.isEmpty() && state.errorMessage == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Press Scan to discover nearby BLE devices",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn {
                    items(displayDevices, key = { it.id }) { device ->
                        DeviceCard(
                            device = device,
                            onClick = { onDeviceClick(device) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: UiBleDevice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "RSSI: ${device.rssi} dBm  •  ${device.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
