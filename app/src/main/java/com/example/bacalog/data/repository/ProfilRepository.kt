package com.example.bacalog.data.repository

import com.example.bacalog.data.model.FokusDto
import com.example.bacalog.data.model.FokusRequest
import com.example.bacalog.data.model.UpdateProfilRequest
import com.example.bacalog.data.model.UserProfilDto
import com.example.bacalog.data.remote.ApiService
import org.json.JSONObject

class ProfilRepository(private val apiService: ApiService) {

    suspend fun getProfil(search: String? = null): Result<UserProfilDto> {
        return try {
            val response = apiService.getProfil(search)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfil(
        nama: String,
        bioBelajar: String?,
        passwordLama: String? = null,
        passwordBaru: String? = null
    ): Result<String> {
        return try {
            val req = UpdateProfilRequest(
                nama = nama,
                bio_belajar = bioBelajar,
                password_lama = passwordLama,
                password_baru = passwordBaru
            )
            val response = apiService.updateProfil(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.pesan)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFokusList(search: String? = null): Result<List<FokusDto>> {
        return try {
            val response = apiService.getFokusList(search)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFokus(
        fokusMateri: String,
        keterangan: String?,
        targetSelesai: String?,
        statusFokus: String = "Aktif"
    ): Result<String> {
        return try {
            val req = FokusRequest(
                fokus_materi = fokusMateri,
                keterangan = keterangan,
                target_selesai = targetSelesai,
                status_fokus = statusFokus
            )
            val response = apiService.createFokus(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.pesan)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFokus(
        id: Int,
        fokusMateri: String,
        keterangan: String?,
        targetSelesai: String?,
        statusFokus: String
    ): Result<String> {
        return try {
            val req = FokusRequest(
                fokus_materi = fokusMateri,
                keterangan = keterangan,
                target_selesai = targetSelesai,
                status_fokus = statusFokus
            )
            val response = apiService.updateFokus(id, req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.pesan)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFokus(id: Int): Result<String> {
        return try {
            val response = apiService.deleteFokus(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.pesan)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.optString("message", "Terjadi kesalahan")
            } else {
                "Terjadi kesalahan pada server"
            }
        } catch (e: Exception) {
            "Terjadi kesalahan komunikasi jaringan"
        }
    }
}
