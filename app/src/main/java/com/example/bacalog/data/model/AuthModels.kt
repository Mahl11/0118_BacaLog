package com.example.bacalog.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val kataSandi: String
)

data class RegisterRequest(
    @SerializedName("nama") val nama: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val kataSandi: String
)

data class AuthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val pesan: String,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val pengguna: UserDto? = null
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("email") val email: String,
    @SerializedName("bio_belajar") val bioBelajar: String? = null
)
