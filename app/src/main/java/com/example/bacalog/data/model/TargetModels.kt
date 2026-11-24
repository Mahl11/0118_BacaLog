package com.example.bacalog.data.model

import com.google.gson.annotations.SerializedName

data class TargetDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("bacaan_id")
    val bacaanId: Int,
    @SerializedName("target_halaman")
    val targetHalaman: Int,
    @SerializedName("deadline")
    val deadline: String,
    @SerializedName("status_target")
    val statusTarget: String,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("judul_bacaan")
    val judulBacaan: String? = null,
    @SerializedName("total_halaman")
    val totalHalaman: Int? = null,
    @SerializedName("status_baca")
    val statusBaca: String? = null,
    @SerializedName("nama_kategori")
    val namaKategori: String? = null,
    @SerializedName("capaian_tertinggi")
    val capaianTertinggi: Int? = 0
)

data class TargetRequest(
    @SerializedName("bacaan_id")
    val bacaanId: Int,
    @SerializedName("target_halaman")
    val targetHalaman: Int,
    @SerializedName("deadline")
    val deadline: String,
    @SerializedName("status_target")
    val statusTarget: String = "Sedang Berjalan"
)

data class TargetListResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<TargetDto> = emptyList()
)
