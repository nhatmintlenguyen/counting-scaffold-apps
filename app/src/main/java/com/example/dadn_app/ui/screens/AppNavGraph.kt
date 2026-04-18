package com.example.dadn_app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                onLoginSuccess = { email ->
                    // Cất email vào két sắt để dùng lâu dài
                    TokenManager.saveTokens(
                        accessToken = TokenManager.accessToken ?: "",
                        refreshToken = TokenManager.refreshToken ?: "",
                        email = email
                    )
                    navController.navigate("${AppRoutes.MAIN}?email=$email") {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                vm                = authVm,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { email ->
                    // Cất email vào két sắt
                    TokenManager.saveTokens(
                        accessToken = TokenManager.accessToken ?: "",
                        refreshToken = TokenManager.refreshToken ?: "",
                        email = email
                    )
                    navController.navigate("${AppRoutes.MAIN}?email=$email") {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = "${AppRoutes.MAIN}?email={email}",
            arguments = listOf(
                navArgument("email") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null 
                }
            )
        ) { backStackEntry ->
            // Ưu tiên lấy email từ két sắt trước, nếu không có mới lấy từ tham số truyền vào
            val email = TokenManager.userEmail ?: backStackEntry.arguments?.getString("email") ?: "user@example.com"
            val scans = remember { mutableStateListOf<ScanRecord>() }
            
            MainScreen(
                scans = scans,
                email = email,
                onAddScan = { newScan -> 
                    scans.add(0, newScan)
                },
                onUpdateScan = { updatedScan ->
                    val index = scans.indexOfFirst { it.id == updatedScan.id }
                    if (index != -1) {
                        scans[index] = updatedScan
                    }
                },
                onLogout = {
                    TokenManager.clearTokens() // Xóa sạch dữ liệu khi đăng xuất
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
