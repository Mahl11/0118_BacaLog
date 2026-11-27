package com.example.bacalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.model.DashboardStatistikDto
import com.example.bacalog.data.model.TargetDto
import com.example.bacalog.data.remote.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveTargetSummary(
    val activeCount: Int = 0,
    val highlightedTarget: TargetDto? = null
)

class HomeViewModel(private val apiService: ApiService) : ViewModel() {

    private val _statistik = MutableStateFlow(DashboardStatistikDto())
    val statistik: StateFlow<DashboardStatistikDto> = _statistik.asStateFlow()

    private val _namaPengguna = MutableStateFlow<String?>(null)
    val namaPengguna: StateFlow<String?> = _namaPengguna.asStateFlow()

    private val _activeTargetSummary = MutableStateFlow(ActiveTargetSummary())
    val activeTargetSummary: StateFlow<ActiveTargetSummary> = _activeTargetSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStatistik()
    }

    fun loadStatistik() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val statDeferred = async { runCatching { apiService.getDashboardStatistik() }.getOrNull() }
                val profilDeferred = async { runCatching { apiService.getProfil() }.getOrNull() }
                val targetDeferred = async { runCatching { apiService.getTargetList() }.getOrNull() }

                val statRes = statDeferred.await()
                if (statRes != null && statRes.isSuccessful && statRes.body() != null) {
                    val body = statRes.body()!!
                    _statistik.value = DashboardStatistikDto(
                        totalBacaan = body.totalBacaan,
                        sedangDibaca = body.sedangDibaca,
                        selesaiDibaca = body.selesaiDibaca,
                        totalHalaman = body.totalHalaman
                    )
                }

                val profilRes = profilDeferred.await()
                if (profilRes != null && profilRes.isSuccessful && profilRes.body() != null) {
                    val namaLengkap = profilRes.body()!!.data.nama.trim()
                    if (namaLengkap.isNotEmpty()) {
                        _namaPengguna.value = namaLengkap.substringBefore(" ")
                    }
                }

                val targetRes = targetDeferred.await()
                if (targetRes != null && targetRes.isSuccessful && targetRes.body() != null) {
                    val allTargets = targetRes.body()!!.data
                    val activeTargets = allTargets.filter {
                        it.statusTarget.equals("Sedang Berjalan", ignoreCase = true)
                    }
                    _activeTargetSummary.value = ActiveTargetSummary(
                        activeCount = activeTargets.size,
                        highlightedTarget = activeTargets.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                // Fallback graceful
            } finally {
                _isLoading.value = false
            }
        }
    }
}
