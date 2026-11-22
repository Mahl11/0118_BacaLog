package com.example.bacalog.data.model

import com.google.gson.annotations.SerializedName

data class DashboardStatistikDto(
    @SerializedName("total_bacaan") val totalBacaan: Int = 0,
    @SerializedName("sedang_dibaca") val sedangDibaca: Int = 0,
    @SerializedName("selesai_dibaca") val selesaiDibaca: Int = 0,
    @SerializedName("total_halaman") val totalHalaman: Int = 0
)

data class DashboardStatistikResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_bacaan") val totalBacaan: Int = 0,
    @SerializedName("sedang_dibaca") val sedangDibaca: Int = 0,
    @SerializedName("selesai_dibaca") val selesaiDibaca: Int = 0,
    @SerializedName("total_halaman") val totalHalaman: Int = 0
)
