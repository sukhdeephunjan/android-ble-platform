package com.example.composeapp.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.composeapp.signup.SignUpViewModel
import com.example.composeapp.utils.SessionManager
import com.example.composeapp.validation.LoginFormValidation
import com.example.datalib.user.UserEntity
@Composable
fun LoginRoute(onLoginSuccess: (UserEntity) -> Unit,
               onSignUpClick: () -> Unit, viewModel: LoginViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is LoginUiEvent.LoginSuccess) {
                onLoginSuccess(event.user)
            }
        }
    }

    LoginScreenUi(
        uiState = uiState.value,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignInClick = viewModel::onSignInClick,
        onSignUpClick = onSignUpClick,
    )
}