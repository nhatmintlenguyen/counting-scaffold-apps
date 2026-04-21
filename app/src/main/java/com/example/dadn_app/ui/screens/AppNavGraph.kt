package com.example.dadn_app.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dadn_app.core.utils.TokenManager
import com.example.dadn_app.ui.viewmodel.AuthViewModel

// ─── Top-level route constants ────────────────────────────────────────────────

object AppRoutes {
    const val LOGIN    = "login"
    const val REGISTER = "register"
    const val MAIN     = "main"
}

/**
 * Root navigation graph for the whole app.
 *
 * [startDestination] is chosen by MainActivity:
 *   - "main"  → user is already logged in (token exists)
 *   - "login" → fresh install or after logout
 *
 * A single [AuthViewModel] instance is shared between LoginScreen and
 * RegisterScreen so they don't reset state when navigating between them.
 */
@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    // Shared AuthViewModel scoped to the NavGraph lifetime
    val authVm: AuthViewModel = viewModel()

    NavHost(
        navController    = navController,
        startDestination = startDestination,
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                vm                   = authVm,
                onNavigateToRegister = {
                    navController.navigate(AppRoutes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(AppRoutes.MAIN) {
                        // Remove all auth screens from the back stack —
                        // pressing Back from MainScreen exits the app
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                vm                = authVm,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoutes.MAIN) {
            MainScreen(
                onLogout = {
                    TokenManager.clearTokens()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
