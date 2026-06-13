package com.example.dadn_app.data.models

import com.google.gson.annotations.SerializedName

// ─── Request bodies ───────────────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
)

data class RegisterRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
)

data class GoogleLoginRequest(
    @SerializedName("google_token") val googleToken: String,
)

// ─── Response bodies ──────────────────────────────────────────────────────────

/**
 * Returned by /login on success.
 * The backend issues a JWT access token that must be sent in protected requests.
 */
data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type")   val tokenType: String = "bearer",
)

data class RegisterResponse(
    @SerializedName("message") val message: String,
)

data class UserDto(
    @SerializedName("id")        val id: Int,
    @SerializedName("email")     val email: String,
)

/**
 * Generic wrapper for error responses from the backend.
 * e.g. { "message": "Invalid credentials" }
 */
data class ApiError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("detail")  val detail: String? = null,
)
