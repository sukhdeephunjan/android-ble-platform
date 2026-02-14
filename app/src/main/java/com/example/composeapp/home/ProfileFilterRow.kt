package com.example.composeapp.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blelib.bleCore.BleProfile

@Composable
fun ProfileFilterRow(
    profiles: List<BleProfile>,
    selectedProfileId: String?,
    onProfileSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // "All devices" chip
        FilterChip(
            selected = selectedProfileId == null,
            onClick = { onProfileSelected(null) },
            label = { Text("All") }
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
