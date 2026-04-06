package com.example.dadn_app.core.network

import com.example.dadn_app.data.models.AuthResponse
import com.example.dadn_app.data.models.LoginRequest
import com.example.dadn_app.data.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>
}
