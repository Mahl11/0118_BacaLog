package com.example.bacalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.model.BacaanDto
import com.example.bacalog.data.model.TargetDto
import com.example.bacalog.data.repository.BacaanRepository
import com.example.bacalog.data.repository.TargetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface TargetUiState {
    object Loading : TargetUiState
    data class Success(val listTarget: List<TargetDto>) : TargetUiState
    object Empty : TargetUiState
    data class Error(val message: String) : TargetUiState
}

class TargetViewModel(
    private val targetRepository: TargetRepository,
    private val bacaanRepository: BacaanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TargetUiState>(TargetUiState.Loading)
    val uiState: StateFlow<TargetUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterStatus = MutableStateFlow("Semua")
    val selectedFilterStatus: StateFlow<String> = _selectedFilterStatus.asStateFlow()

    private val _bacaanList = MutableStateFlow<List<BacaanDto>>(emptyList())
    val bacaanList: StateFlow<List<BacaanDto>> = _bacaanList.asStateFlow()

    private var rawTargetList: List<TargetDto> = emptyList()

    // Dialog state
    private val _isAddEditDialogOpen = MutableStateFlow(false)
    val isAddEditDialogOpen: StateFlow<Boolean> = _isAddEditDialogOpen.asStateFlow()

    private val _targetBeingEdited = MutableStateFlow<TargetDto?>(null)
    val targetBeingEdited: StateFlow<TargetDto?> = _targetBeingEdited.asStateFlow()

    private val _isDeleteDialogOpen = MutableStateFlow(false)
    val isDeleteDialogOpen: StateFlow<Boolean> = _isDeleteDialogOpen.asStateFlow()

    private val _targetToDelete = MutableStateFlow<TargetDto?>(null)
    val targetToDelete: StateFlow<TargetDto?> = _targetToDelete.asStateFlow()

    private val _dialogError = MutableStateFlow<String?>(null)
    val dialogError: StateFlow<String?> = _dialogError.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadBacaan()
        loadTarget()
    }

    fun resetSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun getDefaultDeadlineString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
    }

    fun loadBacaan() {
        viewModelScope.launch {
            val result = bacaanRepository.getBacaanList()
            result.onSuccess { list ->
                _bacaanList.value = list
            }
        }
    }

    fun loadTarget(search: String? = _searchQuery.value) {
        _uiState.value = TargetUiState.Loading
        viewModelScope.launch {
            val result = targetRepository.getTargetList(search = search)
            result.onSuccess { data ->
                rawTargetList = data
                applyFilterAndEmit()
            }.onFailure { throwable ->
                _uiState.value = TargetUiState.Error(throwable.message ?: "Terjadi kesalahan saat memuat target baca")
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            loadTarget(search = newQuery)
        }
    }

    fun onFilterStatusChanged(status: String) {
        _selectedFilterStatus.value = status
        applyFilterAndEmit()
    }

    private fun applyFilterAndEmit() {
        val filter = _selectedFilterStatus.value
        val filtered = if (filter == "Semua") {
            rawTargetList
        } else {
            rawTargetList.filter { it.statusTarget.equals(filter, ignoreCase = true) }
        }

        if (filtered.isEmpty()) {
            _uiState.value = TargetUiState.Empty
        } else {
            _uiState.value = TargetUiState.Success(filtered)
        }
    }

    fun openAddDialog() {
        _dialogError.value = null
        _targetBeingEdited.value = null
        _isAddEditDialogOpen.value = true
        loadBacaan()
    }

    fun openEditDialog(target: TargetDto) {
        _dialogError.value = null
        _targetBeingEdited.value = target
        _isAddEditDialogOpen.value = true
        loadBacaan()
    }

    fun dismissAddEditDialog() {
        _isAddEditDialogOpen.value = false
        _targetBeingEdited.value = null
        _dialogError.value = null
    }

    fun openDeleteDialog(target: TargetDto) {
        _targetToDelete.value = target
        _isDeleteDialogOpen.value = true
    }

    fun dismissDeleteDialog() {
        _isDeleteDialogOpen.value = false
        _targetToDelete.value = null
    }

    fun saveTarget(
        bacaanId: Int,
        targetHalaman: Int,
        deadline: String,
        statusTarget: String
    ) {
        viewModelScope.launch {
            val currentEdited = _targetBeingEdited.value
            if (currentEdited == null) {
                // Create
                val result = targetRepository.createTarget(
                    bacaanId = bacaanId,
                    targetHalaman = targetHalaman,
                    deadline = deadline,
                    statusTarget = statusTarget
                )
                result.onSuccess {
                    dismissAddEditDialog()
                    _snackbarMessage.value = "Target baca berhasil ditambahkan"
                    loadTarget()
                }.onFailure { throwable ->
                    _dialogError.value = throwable.message ?: "Gagal menambahkan target baca"
                }
            } else {
                // Update
                val result = targetRepository.updateTarget(
                    id = currentEdited.id,
                    bacaanId = bacaanId,
                    targetHalaman = targetHalaman,
                    deadline = deadline,
                    statusTarget = statusTarget
                )
                result.onSuccess {
                    dismissAddEditDialog()
                    _snackbarMessage.value = "Target baca berhasil diperbarui"
                    loadTarget()
                }.onFailure { throwable ->
                    _dialogError.value = throwable.message ?: "Gagal memperbarui target baca"
                }
            }
        }
    }

    fun toggleStatus(target: TargetDto) {
        val nextStatus = if (target.statusTarget == "Tercapai") "Sedang Berjalan" else "Tercapai"
        viewModelScope.launch {
            val result = targetRepository.updateTarget(
                id = target.id,
                bacaanId = target.bacaanId,
                targetHalaman = target.targetHalaman,
                deadline = target.deadline,
                statusTarget = nextStatus
            )
            result.onSuccess {
                _snackbarMessage.value = "Status target diubah menjadi: $nextStatus"
                loadTarget()
            }.onFailure { throwable ->
                _snackbarMessage.value = throwable.message ?: "Gagal mengubah status target"
            }
        }
    }

    fun confirmDeleteTarget() {
        val target = _targetToDelete.value ?: return
        viewModelScope.launch {
            val result = targetRepository.deleteTarget(target.id)
            result.onSuccess {
                dismissDeleteDialog()
                _snackbarMessage.value = "Target baca berhasil dihapus"
                loadTarget()
            }.onFailure { throwable ->
                dismissDeleteDialog()
                _snackbarMessage.value = throwable.message ?: "Gagal menghapus target baca"
            }
        }
    }
}
