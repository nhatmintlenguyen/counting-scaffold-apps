package com.example.dadn_app.core.network

import com.example.dadn_app.core.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that attaches the Authorization header to every outgoing
 * request automatically, as long as a token is stored.
 *
 * This means individual API calls never need to manually pass the token —
 * the interceptor handles it transparently for every Retrofit call.
 *
 * Header format: Authorization: Bearer <accessToken>
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.accessToken
        val request = if (token != null) {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
