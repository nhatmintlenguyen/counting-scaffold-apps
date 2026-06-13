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
    const val MAIN     = "main"
}

/**
 * Root navigation graph for the whole app.
 *
 * [startDestination] is chosen by MainActivity:
 *   - "main"  → user is already logged in (token exists)
 *   - "login" → fresh install or after logout
 *
 * A single [AuthViewModel] instance is scoped to the auth flow.
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
                vm = authVm,
                onLoginSuccess = {
                    navController.navigate(AppRoutes.MAIN) {
                        // Remove auth from the back stack so Back exits from Main.
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
