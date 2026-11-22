package com.example.bacalog.data.repository

import com.example.bacalog.data.local.TokenManager
import com.example.bacalog.data.model.AuthResponse
import com.example.bacalog.data.model.GenericResponse
import com.example.bacalog.data.model.LoginRequest
import com.example.bacalog.data.model.RegisterRequest
import com.example.bacalog.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    val accessToken: Flow<String?> = tokenManager.accessToken
    val refreshToken: Flow<String?> = tokenManager.refreshToken

    suspend fun isLoggedIn(): Boolean {
        val token = tokenManager.accessToken.firstOrNull()
        return !token.isNullOrBlank()
    }

    suspend fun login(email: String, kataSandi: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email = email, kataSandi = kataSandi))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (!body.accessToken.isNullOrBlank() && !body.refreshToken.isNullOrBlank()) {
                    tokenManager.saveTokens(body.accessToken, body.refreshToken)
                }
                Result.success(body)
            } else {
                val rawError = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(rawError, "Login gagal (${response.code()})")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(nama: String, email: String, kataSandi: String): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(nama = nama, email = email, kataSandi = kataSandi))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (!body.accessToken.isNullOrBlank() && !body.refreshToken.isNullOrBlank()) {
                    tokenManager.saveTokens(body.accessToken, body.refreshToken)
                }
                Result.success(body)
            } else {
                val rawError = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(rawError, "Registrasi gagal (${response.code()})")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(rawError: String?, fallback: String): String {
        if (rawError.isNullOrBlank()) return fallback
        return try {
            val json = org.json.JSONObject(rawError)
            when {
                json.has("message") -> json.getString("message")
                json.has("pesan") -> json.getString("pesan")
                else -> rawError
            }
        } catch (e: Exception) {
            rawError
        }
    }

    suspend fun logout(): Result<GenericResponse> {
        return try {
            val response = apiService.logout()
            tokenManager.clearTokens()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(GenericResponse(status = "success", pesan = "Berhasil logout lokal"))
            }
        } catch (e: Exception) {
            tokenManager.clearTokens()
            Result.success(GenericResponse(status = "success", pesan = "Berhasil logout lokal"))
        }
    }
}
