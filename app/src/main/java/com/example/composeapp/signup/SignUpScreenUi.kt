package com.example.composeapp.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.composeapp.ui.theme.HeartSpace.Dp_12
import com.example.composeapp.ui.theme.HeartSpace.Dp_16
import com.example.composeapp.ui.theme.HeartSpace.Dp_24
import com.example.composeapp.ui.theme.HeartSpace.Dp_48

@Composable
fun SignUpScreenUi(
    uiState: SignUpUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dp_24),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Sign Up",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dp_16))

        // Email
        OutlinedTextField(
            value = uiState.userEmail,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            isError = uiState.userEmailError != null
        )
        uiState.userEmailError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(Dp_16))

        // Password
        OutlinedTextField(
            value = uiState.userPassword,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.userPasswordError != null
        )
        uiState.userPasswordError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(Dp_16))

        // Confirm Password
        OutlinedTextField(
            value = uiState.userConfirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.userConfirmPasswordError != null
        )
        uiState.userConfirmPasswordError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(Dp_24))

        // Sign Up error (backend or validation)
        uiState.userSignUpError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(Dp_12))
        }

        // Sign Up Button
        Button(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dp_48)
        ) {
            Text("Sign Up", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(Dp_16))

        // Back to Login
        TextButton(onClick = onBackToLoginClick) {
            Text("Already have an account? Login")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreenUi(
        uiState = SignUpUiState(
            userEmail = "",
            userPassword = "",
            userConfirmPassword = "",
            userEmailError = null,
            userPasswordError = null,
            userConfirmPasswordError = null,
            userSignUpError = null
        ),
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onSignUpClick = {},
        onBackToLoginClick = {}
    )
}
