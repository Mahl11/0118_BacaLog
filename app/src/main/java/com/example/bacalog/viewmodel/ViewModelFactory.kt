package com.example.bacalog.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bacalog.data.local.TokenManager
import com.example.bacalog.data.remote.RetrofitClient
import com.example.bacalog.data.repository.AuthRepository

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
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
