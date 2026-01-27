package com.example.composeapp.signup

data class SignUpUiState(
    val userEmail: String = "",
    val userPassword: String = "",
    val userConfirmPassword: String = "",
    val userEmailError: String? = null,
    val userPasswordError: String? = null,
    val userConfirmPasswordError: String? = null,
    val userSignUpError: String? = null
)

sealed class SignupUiEvent {
    object signupSuccess : SignupUiEvent()
}