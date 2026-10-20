package com.example.composeapp.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.composeapp.ui.theme.HeartSpace.Dp_12
import com.example.composeapp.ui.theme.HeartSpace.Dp_16
import com.example.composeapp.ui.theme.HeartSpace.Dp_20
import com.example.composeapp.ui.theme.HeartSpace.Dp_24
import com.example.composeapp.ui.theme.HeartSpace.Dp_4
import com.example.composeapp.ui.theme.HeartSpace.Dp_48
import com.example.composeapp.ui.theme.HeartSpace.Dp_8

@Composable
fun LoginScreenUi(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Dp_24),
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(Dp_8))
        // Subtitle / description
        Text(
            text = "Sign in to continue using the app. Please enter your credentials below.",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(Dp_20))
        // Username / Email
        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            isError = uiState.usernameError != null
        )
        uiState.usernameError?.let { error ->
            Text(text = error, color = Color.Red, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(Dp_16))
        // Password
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.passwordError != null
        )
        uiState.passwordError?.let { error ->
            Text(text = error, color = Color.Red, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(Dp_8))
        // Forgot password
        Text(
            text = "Forgot Password?",
            modifier = Modifier.align(Alignment.End).padding(top = Dp_4),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(Dp_24))
        uiState.loginError?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = Dp_12)
            )
        }
        // Sign In button
        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dp_48),
        ) { Text("Sign In", fontSize = 16.sp) }

        Spacer(modifier = Modifier.height(Dp_24))
        // Sign up text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don’t have an account? ",
                fontSize = 14.sp
            )
            Text(
                text = "Sign Up",
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    onSignUpClick()
                },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreenUi(
        uiState = LoginUiState(),
        onUsernameChange = {},
        onPasswordChange = {},
        onSignInClick = {},
        onSignUpClick = {}
    )
}
