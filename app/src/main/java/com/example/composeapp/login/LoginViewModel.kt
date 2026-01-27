package com.example.composeapp.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.example.composeapp.global.ErrorUiMapper
import com.example.composeapp.global.LoadingManager
import com.example.composeapp.utils.SessionManager
import com.example.composeapp.validation.AuthFormValidation
import com.example.composeapp.validation.LoginFormValidation

import com.example.composeapp.validation.ValidationResult
import com.example.composeapp.validation.ValidationUiMapper
import com.example.datalib.resultHandling.Resource
import com.example.datalib.user.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor( private val userRepository: IUserRepository,
                      private val sessionManager: SessionManager,
                      private val formValidation: LoginFormValidation
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState
    private val _events = MutableSharedFlow<LoginUiEvent>()
    val events = _events.asSharedFlow()

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            username = value,
            usernameError = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null
        )
    }
    fun onSignInClick() {
        val state = _uiState.value
        // Clear previous errors
        _uiState.value = state.copy(
            usernameError = null,
            passwordError = null,
        )

        var hasError = false
        var usernameError: String? = null
        var passwordError: String? = null

        val usernameResult = formValidation.validateUsername(state.username)
        val passwordResult = formValidation.validatePassword(state.password)

        if (usernameResult is ValidationResult.Error) {
            usernameError = ValidationUiMapper.map(usernameResult.type)
            hasError = true
        }

        // Handle password validation
        if (passwordResult is ValidationResult.Error) {
            passwordError = ValidationUiMapper.map(passwordResult.type)
            hasError = true
        }

        // Update UI state & STOP if validation fails
        if (hasError) {
            _uiState.value = state.copy(
                usernameError = usernameError,
                passwordError = passwordError
            )
            return
        }
        // 5️⃣ Clear old errors before API call
        _uiState.value = state.copy(
            usernameError = null,
            passwordError = null,
        )
        //_uiState.value = state.copy(loginError = null) // Reset previous errors

        LoadingManager.isLoading = true
        viewModelScope.launch {
            try {
                val result = userRepository.login(
                    state.username,
                    state.password
                )
                when (result) {
                    is Resource.Success -> {
                        //EMIT EVENT (do NOT store user in state)
                        _events.emit(
                            LoginUiEvent.LoginSuccess(result.data)
                        )
                        sessionManager.saveUser(
                            userId = result.data.id,
                            username = result.data.username
                        )
                    }

                    is Resource.Failure -> {
                        _uiState.value = state.copy(
                            loginError = ErrorUiMapper.map(result.error)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    loginError = e.message ?: "Something went wrong"
                )
            } finally {
                // Hide global loader
                LoadingManager.isLoading = false
            }
        }
    }
}