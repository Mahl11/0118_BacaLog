package com.example.bacalog.data.repository

import com.example.bacalog.data.model.BacaanDto
import com.example.bacalog.data.model.BacaanRequest
import com.example.bacalog.data.model.GenericResponse
import com.example.bacalog.data.remote.ApiService

class BacaanRepository(private val apiService: ApiService) {

    suspend fun getBacaanList(
        search: String? = null,
        kategoriId: String? = null,
        statusBaca: String? = null
    ): Result<List<BacaanDto>> {
        return try {
            val response = apiService.getBacaanList(search, kategoriId, statusBaca)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal memuat bahan bacaan")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBacaan(
        kategoriId: Int,
        judul: String,
        penulisSumber: String,
        totalHalaman: Int,
        statusBaca: String
    ): Result<GenericResponse> {
        return try {
            val request = BacaanRequest(
                kategoriId = kategoriId,
                judul = judul,
                penulisSumber = penulisSumber,
                totalHalaman = totalHalaman,
                statusBaca = statusBaca
            )
            val response = apiService.createBacaan(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal menambahkan bahan bacaan")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBacaan(
        id: Int,
        kategoriId: Int,
        judul: String,
        penulisSumber: String,
        totalHalaman: Int,
        statusBaca: String
    ): Result<GenericResponse> {
        return try {
            val request = BacaanRequest(
                kategoriId = kategoriId,
                judul = judul,
                penulisSumber = penulisSumber,
                totalHalaman = totalHalaman,
                statusBaca = statusBaca
            )
            val response = apiService.updateBacaan(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal memperbarui bahan bacaan")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBacaan(id: Int): Result<GenericResponse> {
        return try {
            val response = apiService.deleteBacaan(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal menghapus bahan bacaan")
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
