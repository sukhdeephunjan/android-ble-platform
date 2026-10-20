package com.example.composeapp.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.blelib.bleCore.BleProfile
import com.example.composeapp.R
import com.example.composeapp.ui.theme.HeartSpace.Dp_12
import com.example.composeapp.ui.theme.HeartSpace.Dp_8

@Composable
fun ProfileFilterRow(
    profiles: List<BleProfile>,
    selectedProfileId: String?,
    onProfileSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dp_12),
        horizontalArrangement = Arrangement.spacedBy(Dp_8)
    ) {
        // "All devices" chip
        FilterChip(
            selected = selectedProfileId == null,
            onClick = { onProfileSelected(null) },
            label = { Text(stringResource(R.string.filter_all)) }
        )

        // Profile-based chips
        profiles.forEach { profile ->
            FilterChip(
                selected = selectedProfileId == profile.id,
                onClick = { onProfileSelected(profile.id) },
                label = { Text(profile.displayName) }
            )
        }
    }
}
