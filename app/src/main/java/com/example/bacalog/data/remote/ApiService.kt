package com.example.bacalog.data.remote

import com.example.bacalog.data.model.AuthResponse
import com.example.bacalog.data.model.GenericResponse
import com.example.bacalog.data.model.LoginRequest
import com.example.bacalog.data.model.RefreshTokenRequest
import com.example.bacalog.data.model.RefreshTokenResponse
import com.example.bacalog.data.model.RegisterRequest
import com.example.bacalog.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<GenericResponse>

    @GET("profil")
    suspend fun getProfile(): Response<UserDto>

    @GET("dashboard/statistik")
    suspend fun getDashboardStatistik(): Response<com.example.bacalog.data.model.DashboardStatistikResponse>
}
