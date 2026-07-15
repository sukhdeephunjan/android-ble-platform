package com.example.composeapp.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composeapp.Screen
import com.example.composeapp.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    sessionManager: SessionManager
) : ViewModel() {
    val destination: StateFlow<Screen?> =
        sessionManager.userFlow
            .map { user ->
                if (user != null) Screen.Permission else Screen.Login
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )
}
