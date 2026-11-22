package com.example.bacalog.data.remote

import com.example.bacalog.data.model.AuthResponse
import com.example.bacalog.data.model.LoginRequest
import com.example.bacalog.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
