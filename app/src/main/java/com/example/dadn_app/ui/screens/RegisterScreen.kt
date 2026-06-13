package com.example.dadn_app.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dadn_app.ui.viewmodel.AuthViewModel

/**
 * Registration is handled by LoginScreen through Google Sign-In.
 * This wrapper remains only for source compatibility with old previews/call sites.
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    LoginScreen(
        vm = vm,
        onLoginSuccess = onRegisterSuccess,
    )
}
