package com.example.bacalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.model.BacaanDto
import com.example.bacalog.data.model.ProgresDto
import com.example.bacalog.data.repository.BacaanRepository
import com.example.bacalog.data.repository.ProgresRepository
import com.example.bacalog.util.ValidationUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ProgresUiState {
    object Loading : ProgresUiState
    data class Success(val listProgres: List<ProgresDto>) : ProgresUiState
    object Empty : ProgresUiState
    data class Error(val message: String) : ProgresUiState
}

class ProgresViewModel(
    private val progresRepository: ProgresRepository,
    private val bacaanRepository: BacaanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgresUiState>(ProgresUiState.Loading)
    val uiState: StateFlow<ProgresUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _bacaanList = MutableStateFlow<List<BacaanDto>>(emptyList())
    val bacaanList: StateFlow<List<BacaanDto>> = _bacaanList.asStateFlow()

    // Dialog state
    private val _isAddEditDialogOpen = MutableStateFlow(false)
    val isAddEditDialogOpen: StateFlow<Boolean> = _isAddEditDialogOpen.asStateFlow()

    private val _progresBeingEdited = MutableStateFlow<ProgresDto?>(null)
    val progresBeingEdited: StateFlow<ProgresDto?> = _progresBeingEdited.asStateFlow()

    private val _isDeleteDialogOpen = MutableStateFlow(false)
    val isDeleteDialogOpen: StateFlow<Boolean> = _isDeleteDialogOpen.asStateFlow()

    private val _progresToDelete = MutableStateFlow<ProgresDto?>(null)
    val progresToDelete: StateFlow<ProgresDto?> = _progresToDelete.asStateFlow()

    private val _dialogError = MutableStateFlow<String?>(null)
    val dialogError: StateFlow<String?> = _dialogError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadBacaan()
        loadProgres()
    }

    fun resetSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun loadBacaan() {
        viewModelScope.launch {
            val result = bacaanRepository.getBacaanList()
            result.onSuccess { list ->
                _bacaanList.value = list
            }
        }
    }

    fun loadProgres(search: String? = _searchQuery.value) {
        _uiState.value = ProgresUiState.Loading
        viewModelScope.launch {
            val result = progresRepository.getProgresList(search = search)
            result.onSuccess { data ->
                if (data.isEmpty()) {
                    _uiState.value = ProgresUiState.Empty
                } else {
                    _uiState.value = ProgresUiState.Success(data)
                }
            }.onFailure { throwable ->
                _uiState.value = ProgresUiState.Error(throwable.message ?: "Terjadi kesalahan saat memuat catatan progres")
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadProgres(search = newQuery)
        }
    }

    fun openAddDialog() {
        loadBacaan()
        _progresBeingEdited.value = null
        _dialogError.value = null
        _isAddEditDialogOpen.value = true
    }

    fun openEditDialog(progres: ProgresDto) {
        loadBacaan()
        _progresBeingEdited.value = progres
        _dialogError.value = null
        _isAddEditDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditDialogOpen.value = false
        _progresBeingEdited.value = null
        _dialogError.value = null
    }

    fun saveProgres(
        bacaanId: Int?,
        tanggalBaca: String,
        halamanDicapaiStr: String,
        ringkasanMateri: String,
        targetHarianStr: String
    ) {
        if (bacaanId == null || bacaanId <= 0) {
            _dialogError.value = "Pilih bahan bacaan terlebih dahulu"
            return
        }
        if (tanggalBaca.trim().isBlank()) {
            _dialogError.value = "Tanggal sesi belajar tidak boleh kosong"
            return
        }
        if (!ValidationUtils.isValidIsoDate(tanggalBaca)) {
            _dialogError.value = "Format tanggal baca harus YYYY-MM-DD (contoh: 2026-11-23)"
            return
        }
        val hal = halamanDicapaiStr.trim().toIntOrNull()
        if (hal == null || hal <= 0) {
            _dialogError.value = "Halaman dicapai harus berupa angka lebih dari 0"
            return
        }
        if (ringkasanMateri.trim().isBlank()) {
            _dialogError.value = "Ringkasan materi tidak boleh kosong"
            return
        }
        val target = targetHarianStr.trim().toIntOrNull() ?: hal

        viewModelScope.launch {
            val targetProgres = _progresBeingEdited.value
            val result = if (targetProgres == null) {
                progresRepository.createProgres(
                    bacaanId = bacaanId,
                    tanggalBaca = tanggalBaca.trim(),
                    halamanDicapai = hal,
                    ringkasanMateri = ringkasanMateri.trim(),
                    targetHarian = target
                )
            } else {
                progresRepository.updateProgres(
                    id = targetProgres.id,
                    bacaanId = bacaanId,
                    tanggalBaca = tanggalBaca.trim(),
                    halamanDicapai = hal,
                    ringkasanMateri = ringkasanMateri.trim(),
                    targetHarian = target
                )
            }

            result.onSuccess {
                val action = if (targetProgres == null) "ditambahkan" else "diperbarui"
                closeAddEditDialog()
                _snackbarMessage.value = "Catatan progres berhasil $action"
                loadProgres()
            }.onFailure { throwable ->
                _dialogError.value = throwable.message ?: "Gagal menyimpan catatan progres"
            }
        }
    }

    fun openDeleteDialog(progres: ProgresDto) {
        _progresToDelete.value = progres
        _isDeleteDialogOpen.value = true
    }

    fun closeDeleteDialog() {
        _isDeleteDialogOpen.value = false
        _progresToDelete.value = null
    }

    fun confirmDelete() {
        val target = _progresToDelete.value ?: return
        viewModelScope.launch {
            val result = progresRepository.deleteProgres(target.id)
            result.onSuccess {
                closeDeleteDialog()
                _snackbarMessage.value = "Catatan progres berhasil dihapus"
                loadProgres()
            }.onFailure { throwable ->
                closeDeleteDialog()
                _uiState.value = ProgresUiState.Error(throwable.message ?: "Gagal menghapus catatan progres")
            }
        }
    }
}
