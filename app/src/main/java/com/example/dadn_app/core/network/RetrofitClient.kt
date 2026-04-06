package com.example.dadn_app.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit instance shared across the app.
 *
 * HOW TO CONFIGURE:
 *   Change BASE_URL to point to your backend server.
 *   For a local backend on the same machine as the emulator use: "http://10.0.2.2:8000/"
 *   For a real server use the full URL:                          "https://api.yourserver.com/"
 */
object RetrofitClient {

    // ← Change this to your server address
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY   // logs full request + response in Logcat
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())     // attaches Bearer token on every call
        .addInterceptor(loggingInterceptor)    // logs traffic for debugging
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApiService by lazy {
        instance.create(AuthApiService::class.java)
    }
}
