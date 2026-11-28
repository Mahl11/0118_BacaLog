package com.example.bacalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.model.FokusDto
import com.example.bacalog.data.model.UserProfilDto
import com.example.bacalog.data.repository.ProfilRepository
import com.example.bacalog.util.ValidationUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfilUiState {
    object Loading : ProfilUiState
    data class Success(val profil: UserProfilDto) : ProfilUiState
    data class Error(val message: String) : ProfilUiState
}

sealed interface FokusUiState {
    object Loading : FokusUiState
    data class Success(val listFokus: List<FokusDto>) : FokusUiState
    object Empty : FokusUiState
    data class Error(val message: String) : FokusUiState
}

class ProfilViewModel(
    private val profilRepository: ProfilRepository
) : ViewModel() {

    private val _profilUiState = MutableStateFlow<ProfilUiState>(ProfilUiState.Loading)
    val profilUiState: StateFlow<ProfilUiState> = _profilUiState.asStateFlow()

    private val _fokusUiState = MutableStateFlow<FokusUiState>(FokusUiState.Loading)
    val fokusUiState: StateFlow<FokusUiState> = _fokusUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("Semua")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    // Dialog state for edit profil
    private val _isEditProfilOpen = MutableStateFlow(false)
    val isEditProfilOpen: StateFlow<Boolean> = _isEditProfilOpen.asStateFlow()

    // Dialog state for add/edit fokus
    private val _isAddEditFokusOpen = MutableStateFlow(false)
    val isAddEditFokusOpen: StateFlow<Boolean> = _isAddEditFokusOpen.asStateFlow()

    private val _fokusBeingEdited = MutableStateFlow<FokusDto?>(null)
    val fokusBeingEdited: StateFlow<FokusDto?> = _fokusBeingEdited.asStateFlow()

    // Dialog state for delete fokus
    private val _isDeleteFokusOpen = MutableStateFlow(false)
    val isDeleteFokusOpen: StateFlow<Boolean> = _isDeleteFokusOpen.asStateFlow()

    private val _fokusToDelete = MutableStateFlow<FokusDto?>(null)
    val fokusToDelete: StateFlow<FokusDto?> = _fokusToDelete.asStateFlow()

    // Dialog logout
    private val _isLogoutDialogOpen = MutableStateFlow(false)
    val isLogoutDialogOpen: StateFlow<Boolean> = _isLogoutDialogOpen.asStateFlow()

    private val _dialogError = MutableStateFlow<String?>(null)
    val dialogError: StateFlow<String?> = _dialogError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadProfil()
        loadFokus()
    }

    fun resetSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun loadProfil() {
        _profilUiState.value = ProfilUiState.Loading
        viewModelScope.launch {
            val result = profilRepository.getProfil()
            result.onSuccess { user ->
                _profilUiState.value = ProfilUiState.Success(user)
            }.onFailure { err ->
                _profilUiState.value = ProfilUiState.Error(err.message ?: "Gagal memuat profil pengguna")
            }
        }
    }

    fun loadFokus(
        search: String? = _searchQuery.value,
        statusFilter: String? = _selectedStatusFilter.value
    ) {
        _fokusUiState.value = FokusUiState.Loading
        viewModelScope.launch {
            val result = profilRepository.getFokusList(search)
            result.onSuccess { allList ->
                val filtered = if (statusFilter == null || statusFilter == "Semua") {
                    allList
                } else {
                    allList.filter { it.status_fokus.equals(statusFilter, ignoreCase = true) }
                }

                if (filtered.isEmpty()) {
                    _fokusUiState.value = FokusUiState.Empty
                } else {
                    _fokusUiState.value = FokusUiState.Success(filtered)
                }
            }.onFailure { err ->
                _fokusUiState.value = FokusUiState.Error(err.message ?: "Gagal memuat catatan fokus")
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadFokus(search = newQuery)
        }
    }

    fun onStatusFilterSelected(status: String) {
        _selectedStatusFilter.value = status
        loadFokus(statusFilter = status)
    }

    fun openEditProfilDialog() {
        _dialogError.value = null
        _isEditProfilOpen.value = true
    }

    fun closeEditProfilDialog() {
        _isEditProfilOpen.value = false
        _dialogError.value = null
    }

    fun saveProfil(
        nama: String,
        bio: String?,
        passwordLama: String?,
        passwordBaru: String?
    ) {
        if (nama.trim().isBlank()) {
            _dialogError.value = "Nama lengkap tidak boleh kosong"
            return
        }
        if (!passwordBaru.isNullOrBlank()) {
            if (passwordLama.isNullOrBlank()) {
                _dialogError.value = "Kata sandi lama wajib diisi untuk mengganti kata sandi"
                return
            }
            if (passwordBaru.trim().length < 6) {
                _dialogError.value = "Kata sandi baru minimal 6 karakter"
                return
            }
        }

        viewModelScope.launch {
            val result = profilRepository.updateProfil(
                nama = nama.trim(),
                bioBelajar = bio?.trim()?.ifBlank { null },
                passwordLama = passwordLama?.trim()?.ifBlank { null },
                passwordBaru = passwordBaru?.trim()?.ifBlank { null }
            )
            result.onSuccess { msg ->
                _snackbarMessage.value = msg
                closeEditProfilDialog()
                loadProfil()
            }.onFailure { err ->
                _dialogError.value = err.message ?: "Gagal memperbarui profil"
            }
        }
    }

    fun openAddFokusDialog() {
        _fokusBeingEdited.value = null
        _dialogError.value = null
        _isAddEditFokusOpen.value = true
    }

    fun openEditFokusDialog(fokus: FokusDto) {
        _fokusBeingEdited.value = fokus
        _dialogError.value = null
        _isAddEditFokusOpen.value = true
    }

    fun closeAddEditFokusDialog() {
        _isAddEditFokusOpen.value = false
        _fokusBeingEdited.value = null
        _dialogError.value = null
    }

    fun saveFokus(
        fokusMateri: String,
        keterangan: String?,
        targetSelesai: String?,
        statusFokus: String
    ) {
        if (fokusMateri.trim().isBlank()) {
            _dialogError.value = "Materi fokus belajar tidak boleh kosong"
            return
        }
        val targetTrimmed = targetSelesai?.trim()?.ifBlank { null }
        if (targetTrimmed != null && !ValidationUtils.isValidIsoDate(targetTrimmed)) {
            _dialogError.value = "Format target selesai harus YYYY-MM-DD (contoh: 2026-11-30)"
            return
        }

        viewModelScope.launch {
            val edited = _fokusBeingEdited.value
            val result = if (edited == null) {
                profilRepository.createFokus(
                    fokusMateri = fokusMateri.trim(),
                    keterangan = keterangan?.trim()?.ifBlank { null },
                    targetSelesai = targetSelesai?.trim()?.ifBlank { null },
                    statusFokus = statusFokus
                )
            } else {
                profilRepository.updateFokus(
                    id = edited.id,
                    fokusMateri = fokusMateri.trim(),
                    keterangan = keterangan?.trim()?.ifBlank { null },
                    targetSelesai = targetSelesai?.trim()?.ifBlank { null },
                    statusFokus = statusFokus
                )
            }

            result.onSuccess { msg ->
                _snackbarMessage.value = msg
                closeAddEditFokusDialog()
                loadFokus()
            }.onFailure { err ->
                _dialogError.value = err.message ?: "Gagal menyimpan catatan fokus"
            }
        }
    }

    fun toggleFokusStatus(fokus: FokusDto) {
        val newStatus = if (fokus.status_fokus == "Aktif") "Selesai" else "Aktif"
        viewModelScope.launch {
            val result = profilRepository.updateFokus(
                id = fokus.id,
                fokusMateri = fokus.fokus_materi,
                keterangan = fokus.keterangan,
                targetSelesai = fokus.target_selesai,
                statusFokus = newStatus
            )
            result.onSuccess {
                _snackbarMessage.value = "Status fokus diubah menjadi $newStatus"
                loadFokus()
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "Gagal mengubah status fokus"
            }
        }
    }

    fun openDeleteFokusDialog(fokus: FokusDto) {
        _fokusToDelete.value = fokus
        _isDeleteFokusOpen.value = true
    }

    fun closeDeleteFokusDialog() {
        _isDeleteFokusOpen.value = false
        _fokusToDelete.value = null
    }

    fun confirmDeleteFokus() {
        val target = _fokusToDelete.value ?: return
        viewModelScope.launch {
            val result = profilRepository.deleteFokus(target.id)
            result.onSuccess { msg ->
                _snackbarMessage.value = msg
                closeDeleteFokusDialog()
                loadFokus()
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "Gagal menghapus catatan fokus"
                closeDeleteFokusDialog()
            }
        }
    }

    fun openLogoutDialog() {
        _isLogoutDialogOpen.value = true
    }

    fun closeLogoutDialog() {
        _isLogoutDialogOpen.value = false
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        closeLogoutDialog()
        onLogoutSuccess()
    }
}
