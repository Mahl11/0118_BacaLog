package com.example.bacalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.model.BacaanDto
import com.example.bacalog.data.model.KategoriDto
import com.example.bacalog.data.repository.BacaanRepository
import com.example.bacalog.data.repository.KategoriRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BacaanUiState {
    object Loading : BacaanUiState
    data class Success(val listBacaan: List<BacaanDto>) : BacaanUiState
    object Empty : BacaanUiState
    data class Error(val message: String) : BacaanUiState
}

class BacaanViewModel(
    private val bacaanRepository: BacaanRepository,
    private val kategoriRepository: KategoriRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BacaanUiState>(BacaanUiState.Loading)
    val uiState: StateFlow<BacaanUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("Semua")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _kategoriList = MutableStateFlow<List<KategoriDto>>(emptyList())
    val kategoriList: StateFlow<List<KategoriDto>> = _kategoriList.asStateFlow()

    // Dialog state
    private val _isAddEditDialogOpen = MutableStateFlow(false)
    val isAddEditDialogOpen: StateFlow<Boolean> = _isAddEditDialogOpen.asStateFlow()

    private val _bacaanBeingEdited = MutableStateFlow<BacaanDto?>(null)
    val bacaanBeingEdited: StateFlow<BacaanDto?> = _bacaanBeingEdited.asStateFlow()

    private val _isDeleteDialogOpen = MutableStateFlow(false)
    val isDeleteDialogOpen: StateFlow<Boolean> = _isDeleteDialogOpen.asStateFlow()

    private val _bacaanToDelete = MutableStateFlow<BacaanDto?>(null)
    val bacaanToDelete: StateFlow<BacaanDto?> = _bacaanToDelete.asStateFlow()

    private val _dialogError = MutableStateFlow<String?>(null)
    val dialogError: StateFlow<String?> = _dialogError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadKategori()
        loadBacaan()
    }

    fun resetSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun loadKategori() {
        viewModelScope.launch {
            val result = kategoriRepository.getKategoriList()
            result.onSuccess { list ->
                _kategoriList.value = list
            }
        }
    }

    fun loadBacaan(
        search: String? = _searchQuery.value,
        statusBaca: String? = _selectedStatusFilter.value
    ) {
        _uiState.value = BacaanUiState.Loading
        viewModelScope.launch {
            val filterStatus = if (statusBaca == "Semua") null else statusBaca
            val result = bacaanRepository.getBacaanList(
                search = search,
                kategoriId = null,
                statusBaca = filterStatus
            )
            result.onSuccess { data ->
                if (data.isEmpty()) {
                    _uiState.value = BacaanUiState.Empty
                } else {
                    _uiState.value = BacaanUiState.Success(data)
                }
            }.onFailure { throwable ->
                _uiState.value = BacaanUiState.Error(throwable.message ?: "Terjadi kesalahan saat memuat bahan bacaan")
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadBacaan(search = newQuery)
        }
    }

    fun onStatusFilterSelected(status: String) {
        _selectedStatusFilter.value = status
        loadBacaan(statusBaca = status)
    }

    fun openAddDialog() {
        loadKategori()
        _bacaanBeingEdited.value = null
        _dialogError.value = null
        _isAddEditDialogOpen.value = true
    }

    fun openEditDialog(bacaan: BacaanDto) {
        loadKategori()
        _bacaanBeingEdited.value = bacaan
        _dialogError.value = null
        _isAddEditDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditDialogOpen.value = false
        _bacaanBeingEdited.value = null
        _dialogError.value = null
    }

    fun saveBacaan(
        kategoriId: Int?,
        judul: String,
        penulisSumber: String,
        totalHalamanStr: String,
        statusBaca: String
    ) {
        if (kategoriId == null || kategoriId <= 0) {
            _dialogError.value = "Pilih kategori topik terlebih dahulu"
            return
        }
        if (judul.trim().isBlank()) {
            _dialogError.value = "Judul bahan bacaan tidak boleh kosong"
            return
        }
        if (penulisSumber.trim().isBlank()) {
            _dialogError.value = "Penulis atau sumber belajar tidak boleh kosong"
            return
        }
        val totalHal = totalHalamanStr.trim().toIntOrNull()
        if (totalHal == null || totalHal <= 0) {
            _dialogError.value = "Total halaman harus berupa angka lebih dari 0"
            return
        }

        viewModelScope.launch {
            val target = _bacaanBeingEdited.value
            val result = if (target == null) {
                bacaanRepository.createBacaan(
                    kategoriId = kategoriId,
                    judul = judul.trim(),
                    penulisSumber = penulisSumber.trim(),
                    totalHalaman = totalHal,
                    statusBaca = statusBaca
                )
            } else {
                bacaanRepository.updateBacaan(
                    id = target.id,
                    kategoriId = kategoriId,
                    judul = judul.trim(),
                    penulisSumber = penulisSumber.trim(),
                    totalHalaman = totalHal,
                    statusBaca = statusBaca
                )
            }

            result.onSuccess {
                val action = if (target == null) "ditambahkan" else "diperbarui"
                closeAddEditDialog()
                _snackbarMessage.value = "Bahan bacaan berhasil $action"
                loadBacaan()
            }.onFailure { throwable ->
                _dialogError.value = throwable.message ?: "Gagal menyimpan bahan bacaan"
            }
        }
    }

    fun openDeleteDialog(bacaan: BacaanDto) {
        _bacaanToDelete.value = bacaan
        _isDeleteDialogOpen.value = true
    }

    fun closeDeleteDialog() {
        _isDeleteDialogOpen.value = false
        _bacaanToDelete.value = null
    }

    fun confirmDelete() {
        val target = _bacaanToDelete.value ?: return
        viewModelScope.launch {
            val result = bacaanRepository.deleteBacaan(target.id)
            result.onSuccess {
                closeDeleteDialog()
                _snackbarMessage.value = "Bahan bacaan berhasil dihapus"
                loadBacaan()
            }.onFailure { throwable ->
                closeDeleteDialog()
                _uiState.value = BacaanUiState.Error(throwable.message ?: "Gagal menghapus bahan bacaan")
            }
        }
    }
}
