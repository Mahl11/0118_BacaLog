package com.example.bacalog.data.repository

import com.example.bacalog.data.model.GenericResponse
import com.example.bacalog.data.model.ProgresDto
import com.example.bacalog.data.model.ProgresRequest
import com.example.bacalog.data.remote.ApiService

class ProgresRepository(private val apiService: ApiService) {

    suspend fun getProgresList(
        search: String? = null,
        bacaanId: String? = null
    ): Result<List<ProgresDto>> {
        return try {
            val response = apiService.getProgresList(search, bacaanId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal memuat catatan progres")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProgres(
        bacaanId: Int,
        tanggalBaca: String,
        halamanDicapai: Int,
        ringkasanMateri: String,
        targetHarian: Int
    ): Result<GenericResponse> {
        return try {
            val request = ProgresRequest(
                bacaanId = bacaanId,
                tanggalBaca = tanggalBaca,
                halamanDicapai = halamanDicapai,
                ringkasanMateri = ringkasanMateri,
                targetHarian = targetHarian
            )
            val response = apiService.createProgres(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal menambahkan catatan progres")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProgres(
        id: Int,
        bacaanId: Int,
        tanggalBaca: String,
        halamanDicapai: Int,
        ringkasanMateri: String,
        targetHarian: Int
    ): Result<GenericResponse> {
        return try {
            val request = ProgresRequest(
                bacaanId = bacaanId,
                tanggalBaca = tanggalBaca,
                halamanDicapai = halamanDicapai,
                ringkasanMateri = ringkasanMateri,
                targetHarian = targetHarian
            )
            val response = apiService.updateProgres(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal memperbarui catatan progres")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProgres(id: Int): Result<GenericResponse> {
        return try {
            val response = apiService.deleteProgres(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal menghapus catatan progres")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(rawError: String?, fallback: String): String {
        if (rawError.isNullOrBlank()) return fallback
        return try {
            val json = org.json.JSONObject(rawError)
            when {
                json.has("message") -> json.getString("message")
                json.has("pesan") -> json.getString("pesan")
                else -> rawError
            }
        } catch (e: Exception) {
            rawError
        }
    }
}
