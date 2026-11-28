package com.example.bacalog.data.remote

import com.example.bacalog.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        private val refreshLock = Any()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Jangan tambahkan header Bearer jika sedang mengakses endpoint autentikasi
        val requestUrl = originalRequest.url.encodedPath
        val isAuthEndpoint = requestUrl.contains("auth/login") ||
                             requestUrl.contains("auth/register") ||
                             requestUrl.contains("auth/refresh")

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

        // Handle 401 Unauthorized (Token kadaluarsa) -> Auto Refresh Token (Thread-Safe Lock)
        if (response.code == 401 && !isAuthEndpoint) {
            synchronized(refreshLock) {
                val latestToken = runBlocking {
                    tokenManager.accessToken.firstOrNull()
                }
                if (!latestToken.isNullOrBlank() && latestToken != token) {
                    response.close()
                    val retryWithLatest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $latestToken")
                        .header("Accept", "application/json")
                        .build()
                    return chain.proceed(retryWithLatest)
                }

                val refreshToken = runBlocking {
                    tokenManager.refreshToken.firstOrNull()
                }

                if (!refreshToken.isNullOrBlank()) {
                    val newAccessToken = refreshAccessToken(originalRequest, refreshToken)
                    if (!newAccessToken.isNullOrBlank()) {
                        runBlocking {
                            tokenManager.updateAccessToken(newAccessToken)
                        }
                        response.close()
                        val retryRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .header("Accept", "application/json")
                            .build()
                        return chain.proceed(retryRequest)
                    }
                }

                // Jika refresh token juga gagal atau tidak ada, bersihkan sesi
                runBlocking {
                    tokenManager.clearTokens()
                }
            }
        }

        return response
    }

    private fun refreshAccessToken(originalRequest: Request, refreshToken: String): String? {
        return try {
            val refreshUrl = originalRequest.url.newBuilder()
                .encodedPath("/api/auth/refresh")
                .build()

            val jsonBody = JSONObject().put("refresh_token", refreshToken).toString()
            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .header("Accept", "application/json")
                .build()

            // Gunakan OkHttpClient mandiri tanpa interceptor untuk menghindari loop rekursif
            val client = OkHttpClient.Builder().build()
            client.newCall(refreshRequest).execute().use { refreshResponse ->
                if (refreshResponse.isSuccessful) {
                    val bodyString = refreshResponse.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val json = JSONObject(bodyString)
                        if (json.has("access_token")) {
                            return json.getString("access_token")
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
