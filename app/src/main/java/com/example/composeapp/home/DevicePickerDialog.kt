package com.example.composeapp.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.blelib.bleCore.BleProfile
import com.example.composeapp.R
import com.example.composeapp.bledeviceui.model.UiBleDevice

@Composable
fun DevicePickerDialog(
    devices: List<UiBleDevice>,
    isScanning: Boolean,
    profiles: List<BleProfile>,
    onDeviceClick: (UiBleDevice) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_select_device)) },
        text = {
            Column {
                // Scanning indicator
                if (isScanning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        SpinningIndicator(size = 16.dp, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.scanning_for_devices),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (devices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                if (isScanning) R.string.looking_for_matching_devices
                                else R.string.no_matching_devices
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(devices, key = { it.id }) { device ->
                            DevicePickerRow(
                                device = device,
                                profiles = profiles,
                                onClick = { onDeviceClick(device) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DevicePickerRow(
    device: UiBleDevice,
    profiles: List<BleProfile>,
    onClick: () -> Unit
) {
    // Map advertised service UUIDs → matching profile display names
    // e.g. device advertising 0000180D → "Heart Rate"
    //      device advertising both     → "Heart Rate, Temperature"
    val serviceNames = profiles
        .filter { profile ->
            profile.services.any { serviceDef ->
                device.serviceUuids.any { it.uppercase() == serviceDef.uuid.uppercase() }
            }
        }
        .joinToString(", ") { it.displayName }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyLarge
            )
            // Show matched service names; if iOS didn't include UUIDs in the
            // advertising packet the device still shows but with no service label
            if (serviceNames.isNotBlank()) {
                Text(
                    text = serviceNames,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = stringResource(R.string.rssi_dbm, device.rssi),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


