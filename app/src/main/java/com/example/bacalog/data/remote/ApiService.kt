package com.example.bacalog.data.remote

import com.example.bacalog.data.model.AuthResponse
import com.example.bacalog.data.model.BacaanListResponse
import com.example.bacalog.data.model.BacaanRequest
import com.example.bacalog.data.model.DashboardStatistikResponse
import com.example.bacalog.data.model.GenericResponse
import com.example.bacalog.data.model.KategoriListResponse
import com.example.bacalog.data.model.KategoriRequest
import com.example.bacalog.data.model.LoginRequest
import com.example.bacalog.data.model.RefreshTokenRequest
import com.example.bacalog.data.model.RefreshTokenResponse
import com.example.bacalog.data.model.RegisterRequest
import com.example.bacalog.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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
    suspend fun getDashboardStatistik(): Response<DashboardStatistikResponse>

    // CRUDS 1: Kategori Topik
    @GET("kategori")
    suspend fun getKategoriList(@Query("search") search: String? = null): Response<KategoriListResponse>

    @POST("kategori")
    suspend fun createKategori(@Body request: KategoriRequest): Response<GenericResponse>

    @PUT("kategori/{id}")
    suspend fun updateKategori(@Path("id") id: Int, @Body request: KategoriRequest): Response<GenericResponse>

    @DELETE("kategori/{id}")
    suspend fun deleteKategori(@Path("id") id: Int): Response<GenericResponse>

    // CRUDS 2: Bahan Bacaan
    @GET("bacaan")
    suspend fun getBacaanList(
        @Query("search") search: String? = null,
        @Query("kategori_id") kategoriId: String? = null,
        @Query("status_baca") statusBaca: String? = null
    ): Response<BacaanListResponse>

    @POST("bacaan")
    suspend fun createBacaan(@Body request: BacaanRequest): Response<GenericResponse>

    @PUT("bacaan/{id}")
    suspend fun updateBacaan(@Path("id") id: Int, @Body request: BacaanRequest): Response<GenericResponse>

    @DELETE("bacaan/{id}")
    suspend fun deleteBacaan(@Path("id") id: Int): Response<GenericResponse>
}
