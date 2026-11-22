package com.example.bacalog.data.model

import com.google.gson.annotations.SerializedName

data class BacaanDto(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("kategori_id")
    val kategoriId: Int = 0,
    @SerializedName("nama_kategori")
    val namaKategori: String = "",
    @SerializedName("judul")
    val judul: String = "",
    @SerializedName("penulis_sumber")
    val penulisSumber: String = "",
    @SerializedName("total_halaman")
    val totalHalaman: Int = 0,
    @SerializedName("status_baca")
    val statusBaca: String = "Belum Dibaca",
    @SerializedName("halaman_terakhir")
    val halamanTerakhir: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class BacaanListResponse(
    @SerializedName("status")
    val status: String = "",
    @SerializedName("data")
    val data: List<BacaanDto> = emptyList()
)

data class BacaanRequest(
    @SerializedName("kategori_id")
    val kategoriId: Int,
    @SerializedName("judul")
    val judul: String,
    @SerializedName("penulis_sumber")
    val penulisSumber: String,
    @SerializedName("total_halaman")
    val totalHalaman: Int,
    @SerializedName("status_baca")
    val statusBaca: String
)
