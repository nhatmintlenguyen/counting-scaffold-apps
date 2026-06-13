package com.example.dadn_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dadn_app.data.repository.AuthRepository
import com.example.dadn_app.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── UI State ─────────────────────────────────────────────────────────────────

/**
 * Represents the complete UI state for both Login and Register screens.
 * The screen observes this and re-draws only when it changes.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,       // non-null triggers an error snackbar
    val isSuccess: Boolean = false,         // true = navigate to MainScreen after login
    val isRegisterSuccess: Boolean = false, // true = return to login after register
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Google Sign-In / Sign-Up ─────────────────────────────────────────────

    fun signInWithGoogle(googleToken: String, email: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = repo.signInWithGoogle(googleToken, email?.trim())) {
                is AuthResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is AuthResult.Error   -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun googleSignInFailed(message: String) {
        _uiState.value = AuthUiState(errorMessage = message)
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = repo.login(email.trim(), password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is AuthResult.Error   -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────

    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = repo.register(fullName.trim(), email.trim(), password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(isRegisterSuccess = true)
                is AuthResult.Error   -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    /** Call after an error has been shown to reset the state. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false, isRegisterSuccess = false)
    }
}
