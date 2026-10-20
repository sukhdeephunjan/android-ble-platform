package com.example.composeapp.global

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.composeapp.ui.theme.HeartSpace.Dp_4
import com.example.composeapp.ui.theme.HeartSpace.Dp_50

@Composable
fun GlobalLoadingIndicator() {
    if (LoadingManager.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dp_50),
                color = Color.White,
                strokeWidth = Dp_4
            )
        }
    }
}
