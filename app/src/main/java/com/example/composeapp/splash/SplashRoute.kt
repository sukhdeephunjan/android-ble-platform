package com.example.composeapp.splash
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.composeapp.Screen


// Minimal UI to avoid blank screen
@Composable
fun SplashRoute(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()

) {
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(destination) {
        destination?.route?.let {
            navController.navigate(it) {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}