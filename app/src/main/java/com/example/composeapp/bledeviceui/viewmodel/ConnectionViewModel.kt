package com.example.composeapp.bledeviceui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composeapp.BleConnector
import com.example.composeapp.bledeviceui.connection.BleConnectionState
import com.example.composeapp.bledeviceui.events.ConnectEvent
import com.example.composeapp.bledeviceui.model.UiBleDevice
import com.example.composeapp.bledeviceui.uistate.ConnectUiState
import com.example.composeapp.bledeviceui.uistate.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val bleConnector: BleConnector
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectUiState())
    val uiState: StateFlow<ConnectUiState> = _uiState.asStateFlow()

    fun onEvent(event: ConnectEvent) {
        when (event) {
            is ConnectEvent.Connect -> connect(event.device)
            ConnectEvent.Disconnect -> disconnect()
            ConnectEvent.Retry -> retry()
        }
    }

    // ---------------- PRIVATE ----------------

    private fun connect(device: UiBleDevice) {
        _uiState.value = ConnectUiState(
            BleConnectionState.Connecting(device)
        )

        viewModelScope.launch {
            val result = bleConnector.connect(device.id)

            _uiState.value = result.fold(
                onSuccess = {
                    ConnectUiState(
                        BleConnectionState.Connected(device)
                    )
                },
                onFailure = {
                    ConnectUiState(
                        BleConnectionState.Error(UiError.ConnectionFailed)
                    )
                }
            )
        }
    }

    private fun disconnect() {
        bleConnector.disconnect()
        _uiState.value = ConnectUiState(BleConnectionState.Idle)
    }

    private fun retry() {
        val current = _uiState.value.state
        if (current is BleConnectionState.Connecting) {
            connect(current.device)
        }
    }
}
