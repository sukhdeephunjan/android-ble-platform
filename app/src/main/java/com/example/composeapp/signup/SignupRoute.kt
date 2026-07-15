package com.example.composeapp.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SignupRoute(
    onSignupSuccess: () -> Unit,
    onBackToLoginClick: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                SignupUiEvent.signupSuccess -> onSignupSuccess()
            }
        }
    }

    SignUpScreenUi(
        uiState = uiState,
        onEmailChange = viewModel::onUserEmailChange,
        onPasswordChange = viewModel::onUSerPasswordChange,
        onConfirmPasswordChange = viewModel::onUserConfirmPasswordChange,
        onSignUpClick = viewModel::onSignUpClick,
        onBackToLoginClick = onBackToLoginClick
    )
}