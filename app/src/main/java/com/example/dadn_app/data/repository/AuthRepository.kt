package com.example.dadn_app.data.repository

import com.example.dadn_app.core.network.RetrofitClient
import com.example.dadn_app.core.utils.TokenManager
import com.example.dadn_app.data.models.ApiError
import com.example.dadn_app.data.models.AuthResponse
import com.example.dadn_app.data.models.LoginRequest
import com.example.dadn_app.data.models.RegisterRequest
import com.example.dadn_app.data.models.UserDto
import com.google.gson.Gson
import kotlinx.coroutines.delay

/**
 * Single source of truth for authentication operations.
 *
 * ── Mock mode ──────────────────────────────────────────────────────────────────
 * Set [MOCK_MODE] = true when you have no backend yet.
 * The fake credentials that will be accepted are:
 *   Email:    test@scp.com
 *   Password: 123456
 * Any other input returns an error response so you can test the error path too.
 * ──────────────────────────────────────────────────────────────────────────────
 */
class AuthRepository {

    companion object {
        /**
         * ← FLIP THIS to switch between mock and real backend.
         *   true  = no network calls, fake responses returned after 1 s delay
         *   false = real HTTP calls to RetrofitClient.BASE_URL
         */
        const val MOCK_MODE = true

        // Hardcoded test credentials accepted by the mock
        private const val MOCK_EMAIL    = "test@scp.com"
        private const val MOCK_PASSWORD = "123456"
    }

    private val api  = RetrofitClient.authApi
    private val gson = Gson()

    // ── Login ─────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): AuthResult {
        if (MOCK_MODE) return mockLogin(email, password)

        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                TokenManager.saveTokens(body.accessToken, body.refreshToken)
                AuthResult.Success(body)
            } else {
                AuthResult.Error(parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────

    suspend fun register(fullName: String, email: String, password: String): AuthResult {
        if (MOCK_MODE) return mockRegister(fullName, email, password)

        return try {
            val response = api.register(RegisterRequest(fullName, email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                TokenManager.saveTokens(body.accessToken, body.refreshToken)
                AuthResult.Success(body)
            } else {
                AuthResult.Error(parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    // ── Mock implementations ──────────────────────────────────────────────────

    private suspend fun mockLogin(email: String, password: String): AuthResult {
        delay(1_000)   // simulate network latency

        return if (email == MOCK_EMAIL && password == MOCK_PASSWORD) {
            val fakeResponse = buildFakeAuthResponse(email, "Test Engineer")
            TokenManager.saveTokens(fakeResponse.accessToken, fakeResponse.refreshToken)
            AuthResult.Success(fakeResponse)
        } else {
            AuthResult.Error("Invalid credentials. Use $MOCK_EMAIL / $MOCK_PASSWORD")
        }
    }

    private suspend fun mockRegister(fullName: String, email: String, password: String): AuthResult {
        delay(1_200)   // simulate network latency

        // Simulate "email already in use" for the reserved mock address
        return if (email == MOCK_EMAIL) {
            AuthResult.Error("Email already in use. Try a different address.")
        } else {
            val fakeResponse = buildFakeAuthResponse(email, fullName)
            TokenManager.saveTokens(fakeResponse.accessToken, fakeResponse.refreshToken)
            AuthResult.Success(fakeResponse)
        }
    }

    private fun buildFakeAuthResponse(email: String, fullName: String) = AuthResponse(
        accessToken  = "mock_access_token_${System.currentTimeMillis()}",
        refreshToken = "mock_refresh_token_${System.currentTimeMillis()}",
        user = UserDto(id = 1, fullName = fullName, email = email),
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun parseErrorMessage(errorJson: String?): String {
        if (errorJson == null) return "Unknown error"
        return try {
            gson.fromJson(errorJson, ApiError::class.java).message
        } catch (e: Exception) {
            "Unknown error"
        }
    }
}

// ─── Result wrapper ───────────────────────────────────────────────────────────

sealed class AuthResult {
    data class Success(val data: AuthResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
