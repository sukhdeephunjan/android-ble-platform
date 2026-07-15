package com.example.composeapp

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composeapp.home.HomeScreen
import com.example.composeapp.home.ScanScreen
import com.example.composeapp.login.LoginRoute
import com.example.composeapp.permission.flow.PermissionFlowHost
import com.example.composeapp.signup.SignupRoute
import com.example.composeapp.splash.SplashRoute
import com.example.composeapp.bledeviceui.viewmodel.ScanViewModel
import com.example.composeapp.bledeviceui.events.ScanEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashRoute(navController)
        }

        composable(Screen.Login.route) {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(Screen.Permission.route) {
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
                    navController.navigate(Screen.Permission.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }, onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen()
        }

        composable(Screen.HomeScan.route) {
            val scanViewModel: ScanViewModel = hiltViewModel()
            ScanScreen(
                uiState = scanViewModel.uiState,
                onProfileSelected = { profileId ->
                    scanViewModel.onEvent(ScanEvent.ProfileSelected(profileId))
                },
                onDeviceClick = { device ->
                    scanViewModel.onEvent(ScanEvent.DeviceClicked(device.id))
                },
                onStartScan = {
                    scanViewModel.onEvent(ScanEvent.StartScan)
                },
                onStopScan = {
                    scanViewModel.onEvent(ScanEvent.StopScan)
                }
            )
        }

        composable(Screen.Permission.route) {
            PermissionFlowHost(
                permissionViewModel = hiltViewModel(),
                permissionFlowViewModel = hiltViewModel(),
                onAllPermissionsGranted = {
                    navController.navigate(Screen.HomeScan.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object HomeScan : Screen("scan")
    object Permission : Screen("permission")

    object Profile : Screen("profile")
    object Login : Screen("login")
    object Signup : Screen("signup")
}
