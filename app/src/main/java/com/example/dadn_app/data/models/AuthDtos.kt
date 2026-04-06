package com.example.dadn_app.data.models

import com.google.gson.annotations.SerializedName

// ─── Request bodies ───────────────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
)

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email")     val email: String,
    @SerializedName("password")  val password: String,
)

// ─── Response bodies ──────────────────────────────────────────────────────────

/**
 * Returned by both /api/login and /api/register on success.
 * The backend issues two JWT tokens:
 *   - accessToken  : short-lived, sent in every API request header
 *   - refreshToken : long-lived, used only to obtain a new accessToken
 */
data class AuthResponse(
    @SerializedName("access_token")  val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user")          val user: UserDto,
)

data class UserDto(
    @SerializedName("id")        val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email")     val email: String,
)

/**
 * Generic wrapper for error responses from the backend.
 * e.g. { "message": "Invalid credentials" }
 */
data class ApiError(
    @SerializedName("message") val message: String,
)
