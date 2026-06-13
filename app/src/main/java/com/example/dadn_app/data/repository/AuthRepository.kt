package com.example.dadn_app.data.repository

import com.example.dadn_app.core.network.RetrofitClient
import com.example.dadn_app.core.utils.TokenManager
import com.example.dadn_app.data.models.ApiError
import com.example.dadn_app.data.models.AuthResponse
import com.example.dadn_app.data.models.GoogleLoginRequest
import com.example.dadn_app.data.models.LoginRequest
import com.example.dadn_app.data.models.RegisterRequest
import com.google.gson.Gson
import kotlinx.coroutines.delay
import retrofit2.Response

/** Single source of truth for authentication operations. */
class AuthRepository {

    companion object {
        /** Set to true only when testing without the backend. */
        const val MOCK_MODE = false

        private const val MOCK_EMAIL = "test@scp.com"
        private const val MOCK_PASSWORD = "123456"
    }

    private val api = RetrofitClient.authApi
    private val gson = Gson()

    suspend fun login(email: String, password: String): AuthResult {
        if (MOCK_MODE) return mockLogin(email, password)

        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return AuthResult.Error("Login failed: empty response")
                TokenManager.saveAccessToken(body.accessToken, email)
                AuthResult.Success("Login successful")
            } else {
                AuthResult.Error(parseErrorMessage(response))
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun signInWithGoogle(googleToken: String, email: String?): AuthResult {
        if (MOCK_MODE) return mockGoogleSignIn(email)

        return try {
            val response = api.googleAuth(GoogleLoginRequest(googleToken))
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return AuthResult.Error("Google sign-in failed: empty response")
                TokenManager.saveAccessToken(body.accessToken, email)
                AuthResult.Success("Google sign-in successful")
            } else {
                AuthResult.Error(parseErrorMessage(response))
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun register(fullName: String, email: String, password: String): AuthResult {
        if (MOCK_MODE) return mockRegister(fullName, email, password)

        return try {
            val response = api.register(RegisterRequest(email, password))
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Registration successful. Please log in."
                AuthResult.Success(message)
            } else {
                AuthResult.Error(parseErrorMessage(response))
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    private suspend fun mockGoogleSignIn(email: String?): AuthResult {
        delay(1_000)
        val fakeResponse = AuthResponse(
            accessToken = "mock_google_access_token_${System.currentTimeMillis()}",
        )
        TokenManager.saveAccessToken(fakeResponse.accessToken, email)
        return AuthResult.Success("Google sign-in successful")
    }

    private suspend fun mockLogin(email: String, password: String): AuthResult {
        delay(1_000)

        return if (email == MOCK_EMAIL && password == MOCK_PASSWORD) {
            val fakeResponse = AuthResponse(
                accessToken = "mock_access_token_${System.currentTimeMillis()}",
            )
            TokenManager.saveAccessToken(fakeResponse.accessToken, email)
            AuthResult.Success("Login successful")
        } else {
            AuthResult.Error("Invalid credentials. Use $MOCK_EMAIL / $MOCK_PASSWORD")
        }
    }

    private suspend fun mockRegister(fullName: String, email: String, password: String): AuthResult {
        delay(1_200)

        return if (email == MOCK_EMAIL) {
            AuthResult.Error("Email already in use. Try a different address.")
        } else {
            AuthResult.Success("Registration successful. Please log in.")
        }
    }

    private fun parseErrorMessage(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()?.trim()
        if (errorBody.isNullOrEmpty()) return "Server error (${response.code()})"

        return try {
            val apiError = gson.fromJson(errorBody, ApiError::class.java)
            apiError.detail ?: apiError.message ?: "Server error (${response.code()})"
        } catch (e: Exception) {
            "Server error (${response.code()}): $errorBody"
        }
    }
}

sealed class AuthResult {
    data class Success(val message: String? = null) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
