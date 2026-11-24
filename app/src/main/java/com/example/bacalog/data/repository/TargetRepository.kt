package com.example.bacalog.data.repository

import com.example.bacalog.data.model.GenericResponse
import com.example.bacalog.data.model.TargetDto
import com.example.bacalog.data.model.TargetRequest
import com.example.bacalog.data.remote.ApiService

class TargetRepository(private val apiService: ApiService) {

    suspend fun getTargetList(search: String? = null): Result<List<TargetDto>> {
        return try {
            val response = apiService.getTargetList(search)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal memuat daftar target baca")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTarget(
        bacaanId: Int,
        targetHalaman: Int,
        deadline: String,
        statusTarget: String = "Sedang Berjalan"
    ): Result<GenericResponse> {
        return try {
            val request = TargetRequest(
                bacaanId = bacaanId,
                targetHalaman = targetHalaman,
                deadline = deadline,
                statusTarget = statusTarget
            )
            val response = apiService.createTarget(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal menambahkan target baca")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTarget(
        id: Int,
        bacaanId: Int,
        targetHalaman: Int,
        deadline: String,
        statusTarget: String
    ): Result<GenericResponse> {
        return try {
            val request = TargetRequest(
                bacaanId = bacaanId,
                targetHalaman = targetHalaman,
                deadline = deadline,
                statusTarget = statusTarget
            )
            val response = apiService.updateTarget(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal memperbarui target baca")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTarget(id: Int): Result<GenericResponse> {
        return try {
            val response = apiService.deleteTarget(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Gagal menghapus target baca")
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
                json.has("error") -> json.getString("error")
                else -> fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }
}
