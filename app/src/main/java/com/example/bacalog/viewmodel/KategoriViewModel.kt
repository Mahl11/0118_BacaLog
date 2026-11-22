package com.example.bacalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.model.KategoriDto
import com.example.bacalog.data.repository.KategoriRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface KategoriUiState {
    object Loading : KategoriUiState
    data class Success(val listKategori: List<KategoriDto>) : KategoriUiState
    object Empty : KategoriUiState
    data class Error(val message: String) : KategoriUiState
}

class KategoriViewModel(private val repository: KategoriRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<KategoriUiState>(KategoriUiState.Loading)
    val uiState: StateFlow<KategoriUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Dialog state
    private val _isAddEditDialogOpen = MutableStateFlow(false)
    val isAddEditDialogOpen: StateFlow<Boolean> = _isAddEditDialogOpen.asStateFlow()

    private val _kategoriBeingEdited = MutableStateFlow<KategoriDto?>(null)
    val kategoriBeingEdited: StateFlow<KategoriDto?> = _kategoriBeingEdited.asStateFlow()

    private val _isDeleteDialogOpen = MutableStateFlow(false)
    val isDeleteDialogOpen: StateFlow<Boolean> = _isDeleteDialogOpen.asStateFlow()

    private val _kategoriToDelete = MutableStateFlow<KategoriDto?>(null)
    val kategoriToDelete: StateFlow<KategoriDto?> = _kategoriToDelete.asStateFlow()

    private val _dialogError = MutableStateFlow<String?>(null)
    val dialogError: StateFlow<String?> = _dialogError.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadKategori()
    }

    fun loadKategori(search: String? = _searchQuery.value) {
        _uiState.value = KategoriUiState.Loading
        viewModelScope.launch {
            val result = repository.getKategoriList(search)
            result.onSuccess { data ->
                if (data.isEmpty()) {
                    _uiState.value = KategoriUiState.Empty
                } else {
                    _uiState.value = KategoriUiState.Success(data)
                }
            }.onFailure { throwable ->
                _uiState.value = KategoriUiState.Error(throwable.message ?: "Terjadi kesalahan saat memuat data")
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce pencarian
            loadKategori(newQuery)
        }
    }

    fun openAddDialog() {
        _kategoriBeingEdited.value = null
        _dialogError.value = null
        _isAddEditDialogOpen.value = true
    }

    fun openEditDialog(kategori: KategoriDto) {
        _kategoriBeingEdited.value = kategori
        _dialogError.value = null
        _isAddEditDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditDialogOpen.value = false
        _kategoriBeingEdited.value = null
        _dialogError.value = null
    }

    fun saveKategori(namaTopik: String, deskripsi: String?) {
        val namaTrimmed = namaTopik.trim()
        if (namaTrimmed.isBlank()) {
            _dialogError.value = "Nama kategori topik tidak boleh kosong"
            return
        }

        viewModelScope.launch {
            val target = _kategoriBeingEdited.value
            val result = if (target == null) {
                repository.createKategori(namaTrimmed, deskripsi?.trim())
            } else {
                repository.updateKategori(target.id, namaTrimmed, deskripsi?.trim())
            }

            result.onSuccess {
                closeAddEditDialog()
                loadKategori()
            }.onFailure { throwable ->
                _dialogError.value = throwable.message ?: "Gagal menyimpan kategori"
            }
        }
    }

    fun openDeleteDialog(kategori: KategoriDto) {
        _kategoriToDelete.value = kategori
        _isDeleteDialogOpen.value = true
    }

    fun closeDeleteDialog() {
        _isDeleteDialogOpen.value = false
        _kategoriToDelete.value = null
    }

    fun confirmDelete() {
        val target = _kategoriToDelete.value ?: return
        viewModelScope.launch {
            val result = repository.deleteKategori(target.id)
            result.onSuccess {
                closeDeleteDialog()
                loadKategori()
            }.onFailure { throwable ->
                closeDeleteDialog()
                _uiState.value = KategoriUiState.Error(throwable.message ?: "Gagal menghapus kategori")
            }
        }
    }
}
