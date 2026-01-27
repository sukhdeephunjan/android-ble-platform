package com.example.composeapp.login

import com.example.datalib.user.UserEntity

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null,
)
sealed class LoginUiEvent {
    data class LoginSuccess(val user: UserEntity) : LoginUiEvent()
}
