package com.example.bacalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.model.DashboardStatistikDto
import com.example.bacalog.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val apiService: ApiService) : ViewModel() {

    private val _statistik = MutableStateFlow(DashboardStatistikDto())
    val statistik: StateFlow<DashboardStatistikDto> = _statistik.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStatistik()
    }

    fun loadStatistik() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getDashboardStatistik()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _statistik.value = DashboardStatistikDto(
                        totalBacaan = body.totalBacaan,
                        sedangDibaca = body.sedangDibaca,
                        selesaiDibaca = body.selesaiDibaca,
                        totalHalaman = body.totalHalaman
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
