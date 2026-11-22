package com.example.bacalog.data.model

import com.google.gson.annotations.SerializedName

data class KategoriDto(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("nama_topik")
    val namaTopik: String = "",
    @SerializedName("deskripsi")
    val deskripsi: String? = null,
    @SerializedName("total_bacaan")
    val totalBacaan: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class KategoriListResponse(
    @SerializedName("status")
    val status: String = "",
    @SerializedName("data")
    val data: List<KategoriDto> = emptyList()
)

data class KategoriRequest(
    @SerializedName("nama_topik")
    val namaTopik: String,
    @SerializedName("deskripsi")
    val deskripsi: String? = null
)
