package com.example.bacalog.data.remote

import com.example.bacalog.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Jangan tambahkan header Bearer jika sedang mengakses endpoint autentikasi
        val requestUrl = originalRequest.url.encodedPath
        val isAuthEndpoint = requestUrl.contains("auth/login") || requestUrl.contains("auth/register")

        val token = if (!isAuthEndpoint) {
            runBlocking {
                tokenManager.accessToken.firstOrNull()
            }
        } else {
            null
        }

        val authenticatedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Accept", "application/json")
                .build()
        }

        val response = chain.proceed(authenticatedRequest)

        // Handle 401 Unauthorized (Token kadaluarsa)
        if (response.code == 401 && !isAuthEndpoint) {
            runBlocking {
                tokenManager.clearTokens()
            }
        }

        return response
    }
}
