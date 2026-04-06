package com.example.dadn_app.data.repository

import com.example.dadn_app.core.network.RetrofitClient
import com.example.dadn_app.core.utils.TokenManager
import com.example.dadn_app.data.models.AuthResponse
import com.example.dadn_app.data.models.LoginRequest
import com.example.dadn_app.data.models.RegisterRequest
import com.google.gson.Gson

/**
 * Single source of truth for authentication operations.
 *
 * Returns a sealed [AuthResult] so the ViewModel never has to deal with
 * raw HTTP responses or exception handling.
 */
class AuthRepository {

    private val api = RetrofitClient.authApi
    private val gson = Gson()

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                TokenManager.saveTokens(body.accessToken, body.refreshToken)
                AuthResult.Success(body)
            } else {
                val msg = parseErrorMessage(response.errorBody()?.string())
                AuthResult.Error(msg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun register(fullName: String, email: String, password: String): AuthResult {
        return try {
            val response = api.register(RegisterRequest(fullName, email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                TokenManager.saveTokens(body.accessToken, body.refreshToken)
                AuthResult.Success(body)
            } else {
                val msg = parseErrorMessage(response.errorBody()?.string())
                AuthResult.Error(msg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    private fun parseErrorMessage(errorJson: String?): String {
        if (errorJson == null) return "Unknown error"
        return try {
            gson.fromJson(errorJson, com.example.dadn_app.data.models.ApiError::class.java).message
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
