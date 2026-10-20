package com.example.composeapp.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.blelib.bleCore.BleProfile
import com.example.composeapp.R
import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.ui.theme.HeartSpace.Dp_12
import com.example.composeapp.ui.theme.HeartSpace.Dp_16
import com.example.composeapp.ui.theme.HeartSpace.Dp_2
import com.example.composeapp.ui.theme.HeartSpace.Dp_20
import com.example.composeapp.ui.theme.HeartSpace.Dp_320
import com.example.composeapp.ui.theme.HeartSpace.Dp_8

/**
 * Lists the peripherals discovered during a scan and lets the user pick one.
 *
 * The list stays live while [isScanning] is true — devices are appended as the
 * scanner reports them, so the dialog never has to be reopened.
 */
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
            Column(modifier = Modifier.fillMaxWidth()) {

                if (profiles.isNotEmpty()) {
                    Text(
                        text = profiles.joinToString { it.displayName },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                when {
                    devices.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = Dp_320)
                                .padding(top = Dp_8)
                        ) {
                            items(devices, key = { it.id }) { device ->
                                DeviceRow(device = device, onClick = { onDeviceClick(device) })
                                HorizontalDivider()
                            }
                        }
                    }

                    isScanning -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dp_16),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dp_12)
                        ) {
                            SpinningIndicator(size = Dp_20, strokeWidth = Dp_2)
                            Text(stringResource(R.string.looking_for_matching_devices))
                        }
                    }

                    else -> {
                        Text(
                            text = stringResource(R.string.no_matching_devices),
                            modifier = Modifier.padding(top = Dp_16)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DeviceRow(
    device: UiBleDevice,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dp_12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = stringResource(R.string.rssi_dbm, device.rssi),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
