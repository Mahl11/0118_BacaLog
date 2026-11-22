package com.example.bacalog.data.repository

import com.example.bacalog.data.model.GenericResponse
import com.example.bacalog.data.model.KategoriDto
import com.example.bacalog.data.model.KategoriRequest
import com.example.bacalog.data.remote.ApiService

class KategoriRepository(private val apiService: ApiService) {

    suspend fun getKategoriList(search: String? = null): Result<List<KategoriDto>> {
        return try {
            val response = apiService.getKategoriList(search)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal memuat kategori")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createKategori(namaTopik: String, deskripsi: String?): Result<GenericResponse> {
        return try {
            val response = apiService.createKategori(KategoriRequest(namaTopik = namaTopik, deskripsi = deskripsi))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal menambahkan kategori")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateKategori(id: Int, namaTopik: String, deskripsi: String?): Result<GenericResponse> {
        return try {
            val response = apiService.updateKategori(id, KategoriRequest(namaTopik = namaTopik, deskripsi = deskripsi))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal memperbarui kategori")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteKategori(id: Int): Result<GenericResponse> {
        return try {
            val response = apiService.deleteKategori(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal menghapus kategori")
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
