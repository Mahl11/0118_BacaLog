package com.example.bacalog.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bacalog.data.local.TokenManager
import com.example.bacalog.data.remote.RetrofitClient
import com.example.bacalog.data.repository.AuthRepository
import com.example.bacalog.data.repository.BacaanRepository
import com.example.bacalog.data.repository.KategoriRepository
import com.example.bacalog.data.repository.ProfilRepository
import com.example.bacalog.data.repository.ProgresRepository
import com.example.bacalog.data.repository.TargetRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appContext = context.applicationContext
        val tokenManager = TokenManager(appContext)
        val apiService = RetrofitClient.getApiService(appContext)

        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                val authRepository = AuthRepository(apiService, tokenManager)
                AuthViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(apiService) as T
            }
            modelClass.isAssignableFrom(KategoriViewModel::class.java) -> {
                val kategoriRepository = KategoriRepository(apiService)
                KategoriViewModel(kategoriRepository) as T
            }
            modelClass.isAssignableFrom(BacaanViewModel::class.java) -> {
                val bacaanRepository = BacaanRepository(apiService)
                val kategoriRepository = KategoriRepository(apiService)
                BacaanViewModel(bacaanRepository, kategoriRepository) as T
            }
            modelClass.isAssignableFrom(ProgresViewModel::class.java) -> {
                val progresRepository = ProgresRepository(apiService)
                val bacaanRepository = BacaanRepository(apiService)
                ProgresViewModel(progresRepository, bacaanRepository) as T
            }
            modelClass.isAssignableFrom(TargetViewModel::class.java) -> {
                val targetRepository = TargetRepository(apiService)
                val bacaanRepository = BacaanRepository(apiService)
                TargetViewModel(targetRepository, bacaanRepository) as T
            }
            modelClass.isAssignableFrom(ProfilViewModel::class.java) -> {
                val profilRepository = ProfilRepository(apiService)
                ProfilViewModel(profilRepository, tokenManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
