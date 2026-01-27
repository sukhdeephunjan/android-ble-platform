package com.example.composeapp.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composeapp.global.ErrorUiMapper
import com.example.composeapp.global.LoadingManager
import com.example.composeapp.utils.SessionManager
import com.example.composeapp.validation.SignupFormValidation
import com.example.composeapp.validation.ValidationResult
import com.example.composeapp.validation.ValidationUiMapper
import com.example.datalib.resultHandling.Resource
import com.example.datalib.user.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: IUserRepository, private val sessionManager: SessionManager,
    val formValidation: SignupFormValidation) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SignupUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(
            userEmail = value,
            userEmailError = null
        )
    }

    fun onUSerPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            userPassword = value,
            userPasswordError = null
        )
    }

    fun onUserConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            userConfirmPassword = value,
            userConfirmPasswordError = null
        )
    }

    fun onSignUpClick() {
        val state = _uiState.value
        // Clear previous errors
        _uiState.value = state.copy(
            userEmailError = null,
            userPasswordError = null,
            userSignUpError = null
        )

        var hasError = false
        var usernameError: String? = null
        var passwordError: String? = null

        val usernameResult = formValidation.validateUsername(state.userEmail)
        val passwordResult = formValidation.validatePassword(state.userPassword)

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
                userEmailError = usernameError,
                userPasswordError = passwordError
            )
            return
        }
        // Clear old errors before API call
        _uiState.value = state.copy(
            userEmailError = null,
            userPasswordError = null,
            userSignUpError = null
        )
        //_uiState.value = state.copy(loginError = null) // Reset previous errors
        LoadingManager.isLoading = true
        viewModelScope.launch {
            try {
                val result = userRepository.saveUser(
                    state.userEmail, state.userPassword
                )
                when (result) {
                    is Resource.Success -> {
                        sessionManager.saveUser(
                            userId = result.data,
                            username = state.userEmail
                        )
                        _uiState.value = state.copy(
                            //isSignUpSuccess = true,
                            userSignUpError = null
                        )
                        _uiEvent.emit(SignupUiEvent.signupSuccess)
                    }

                    is Resource.Failure -> {
                        _uiState.value = state.copy(
                            userSignUpError = ErrorUiMapper.map(result.error)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    userSignUpError = e.message ?: "Something went wrong"
                )
            } finally {
                // Hide global loader
                LoadingManager.isLoading = false
            }
        }
    }
}