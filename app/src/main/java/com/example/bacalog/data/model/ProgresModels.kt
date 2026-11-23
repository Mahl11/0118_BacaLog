package com.example.bacalog.data.model

import com.google.gson.annotations.SerializedName

data class ProgresDto(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("bacaan_id")
    val bacaanId: Int = 0,
    @SerializedName("judul_bacaan")
    val judulBacaan: String = "",
    @SerializedName("total_halaman")
    val totalHalaman: Int = 0,
    @SerializedName("nama_kategori")
    val namaKategori: String = "",
    @SerializedName("tanggal_baca")
    val tanggalBaca: String = "",
    @SerializedName("halaman_dicapai")
    val halamanDicapai: Int = 0,
    @SerializedName("ringkasan_materi")
    val ringkasanMateri: String = "",
    @SerializedName("target_harian")
    val targetHarian: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class ProgresListResponse(
    @SerializedName("status")
    val status: String = "",
    @SerializedName("data")
    val data: List<ProgresDto> = emptyList()
)

data class ProgresRequest(
    @SerializedName("bacaan_id")
    val bacaanId: Int,
    @SerializedName("tanggal_baca")
    val tanggalBaca: String,
    @SerializedName("halaman_dicapai")
    val halamanDicapai: Int,
    @SerializedName("ringkasan_materi")
    val ringkasanMateri: String,
    @SerializedName("target_harian")
    val targetHarian: Int
)
