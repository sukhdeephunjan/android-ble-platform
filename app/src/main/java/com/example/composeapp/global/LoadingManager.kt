package com.example.composeapp.global

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object LoadingManager {
    var isLoading by mutableStateOf(false)
}
