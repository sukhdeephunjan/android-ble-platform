package com.example.composeapp.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blelib.bleCore.BleProfile
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import com.example.composeapp.bledeviceui.model.UiBleDevice

@ExperimentalMaterial3Api
@Composable
fun ScanScreen(
    uiState: ScanUiState,
    profiles: List<BleProfile>,
    onProfileSelected: (String?) -> Unit,
    onDeviceClick: (UiBleDevice) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Devices") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            ProfileFilterRow(
                profiles = profiles,
                selectedProfileId = uiState.selectedProfileId,
                onProfileSelected = onProfileSelected
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn {
                items(uiState.allDevices) { device ->
                    DeviceCard(
                        deviceName = device.name,
                        onClick = { onDeviceClick(device) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    deviceName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            text = deviceName.ifBlank { "Unnamed Device" },
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

