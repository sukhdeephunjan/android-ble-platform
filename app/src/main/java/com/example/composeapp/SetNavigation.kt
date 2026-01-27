package com.example.composeapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composeapp.home.HomeScreen
import com.example.composeapp.login.LoginRoute
import com.example.composeapp.signup.SignUpScreenUi
import com.example.composeapp.signup.SignupRoute
import com.example.composeapp.splash.SplashRoute
import com.example.composeapp.splash.SplashViewModel

@Composable
fun SetNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { backStackEntry ->
            // ⚡ Scope ViewModel to NavBackStackEntry
            SplashRoute(navController)

            // Show splash UI
            //SplashRoute()
        }

        composable(Screen.Login.route) {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }, onSignUpClick = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupRoute(
                onSignupSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }, onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")

    object Profile : Screen("profile")
    object Login : Screen("login")
    object Signup : Screen("signup")
}