package com.example.composeapp.bledeviceui.viewmodel

import com.example.composeapp.bledeviceui.events.ScanEvent
import com.example.composeapp.bledeviceui.uistate.ScanUiState
import kotlinx.coroutines.flow.StateFlow

interface ScanViewModelContract {
    val uiState: StateFlow<ScanUiState>
    fun onEvent(event: ScanEvent)
}