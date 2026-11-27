package com.example.bacalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.model.UserDto
import com.example.bacalog.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: UserDto?, val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            delay(650)
            _isUserLoggedIn.value = authRepository.isLoggedIn()
        }
    }

    fun login(email: String, kataSandi: String) {
        if (email.isBlank() || kataSandi.isBlank()) {
            _uiState.value = AuthUiState.Error("Email dan kata sandi tidak boleh kosong")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Format email tidak valid")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email.trim(), kataSandi)
            result.onSuccess { response ->
                _uiState.value = AuthUiState.Success(response.pengguna, response.pesan)
                _isUserLoggedIn.value = true
            }.onFailure { throwable ->
                _uiState.value = AuthUiState.Error(throwable.message ?: "Terjadi kesalahan saat masuk")
            }
        }
    }

    fun register(nama: String, email: String, kataSandi: String, konfirmasiKataSandi: String) {
        if (nama.isBlank() || email.isBlank() || kataSandi.isBlank() || konfirmasiKataSandi.isBlank()) {
            _uiState.value = AuthUiState.Error("Semua formulir pendaftaran wajib diisi")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Format email tidak valid")
            return
        }

        if (kataSandi.length < 6) {
            _uiState.value = AuthUiState.Error("Kata sandi minimal 6 karakter")
            return
        }

        if (kataSandi != konfirmasiKataSandi) {
            _uiState.value = AuthUiState.Error("Konfirmasi kata sandi tidak cocok")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.register(nama.trim(), email.trim(), kataSandi)
            result.onSuccess { response ->
                _uiState.value = AuthUiState.Success(response.pengguna, response.pesan)
                _isUserLoggedIn.value = true
            }.onFailure { throwable ->
                _uiState.value = AuthUiState.Error(throwable.message ?: "Terjadi kesalahan saat pendaftaran")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isUserLoggedIn.value = false
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
