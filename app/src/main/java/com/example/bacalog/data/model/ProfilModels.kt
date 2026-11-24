package com.example.bacalog.data.model

data class UserProfilDto(
    val id: Int,
    val nama: String,
    val email: String,
    val bio_belajar: String?,
    val catatan_fokus: String?,
    val created_at: String?
)

data class ProfilResponse(
    val status: String,
    val data: UserProfilDto
)

data class UpdateProfilRequest(
    val nama: String,
    val bio_belajar: String?,
    val catatan_fokus: String?,
    val password_lama: String? = null,
    val password_baru: String? = null
)

data class FokusDto(
    val id: Int,
    val user_id: Int,
    val fokus_materi: String,
    val keterangan: String?,
    val target_selesai: String?,
    val status_fokus: String,
    val created_at: String?
)

data class FokusListResponse(
    val status: String,
    val data: List<FokusDto>
)

data class FokusRequest(
    val fokus_materi: String,
    val keterangan: String?,
    val target_selesai: String?,
    val status_fokus: String = "Aktif"
)
