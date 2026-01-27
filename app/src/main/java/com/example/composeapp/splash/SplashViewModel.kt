package com.example.composeapp.splash

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composeapp.Screen
import com.example.composeapp.utils.SessionManager
import com.example.composeapp.validation.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val
    sessionManager: SessionManager
) : ViewModel() {
    val destination: StateFlow<Screen?> =
        sessionManager.userFlow
            .map { user ->
                if (user != null) Screen.Home else Screen.Login
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                initialValue = Screen.Login

            )
}
