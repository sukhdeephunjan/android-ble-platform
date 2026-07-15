package com.example.composeapp.permission.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composeapp.permission.PermissionText
import com.example.composeapp.permission.PermissionType
import com.example.composeapp.permission.permissionstate.PermissionState

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
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(PermissionText.title(permissionType)),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(PermissionText.message(permissionState)),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = if (permissionState == PermissionState.PermanentlyDenied) onOpenSettings
                      else onRequestPermission,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(stringResource(PermissionText.action(permissionState)))
        }
    }
}

