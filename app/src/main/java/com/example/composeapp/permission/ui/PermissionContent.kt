package com.example.composeapp.permission.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.composeapp.permission.PermissionText
import com.example.composeapp.permission.PermissionType
import com.example.composeapp.permission.permissionstate.PermissionState

@Composable
fun PermissionContent(
    permissionType: PermissionType,
    state: PermissionState,
    onRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(
                id = PermissionText.title(permissionType)
            ),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(
                id = PermissionText.message(state)
            )
        )

        Spacer(Modifier.height(24.dp))

        if (state != PermissionState.Granted) {
            Button(onClick = onRequest) {
                Text(
                    text = stringResource(
                        id = PermissionText.action()
                    )
                )
            }
        }
    }
}

