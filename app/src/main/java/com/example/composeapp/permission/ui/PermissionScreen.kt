package com.example.composeapp.permission.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.composeapp.permission.PermissionText
import com.example.composeapp.permission.PermissionType
import com.example.composeapp.permission.permissionstate.PermissionState
import com.example.composeapp.ui.theme.HeartSpace.Dp_16
import com.example.composeapp.ui.theme.HeartSpace.Dp_24
import com.example.composeapp.ui.theme.HeartSpace.Dp_32
import com.example.composeapp.ui.theme.HeartSpace.Dp_48

@Composable
fun PermissionScreen(
    permissionType: PermissionType,
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dp_24),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(PermissionText.title(permissionType)),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dp_16))
        Text(
            text = stringResource(PermissionText.message(permissionState)),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dp_32))
        Button(
            onClick = if (permissionState == PermissionState.PermanentlyDenied) onOpenSettings
                      else onRequestPermission,
            modifier = Modifier.fillMaxWidth().height(Dp_48)
        ) {
            Text(stringResource(PermissionText.action(permissionState)))
        }
    }
}
