package com.example.bacalog.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bacalog.data.local.FileLaporanDto
import com.example.bacalog.data.local.LocalFileHelper
import com.example.bacalog.data.repository.BacaanRepository
import com.example.bacalog.data.repository.ProfilRepository
import com.example.bacalog.data.repository.ProgresRepository
import com.example.bacalog.data.repository.TargetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LaporanViewModel(
    private val bacaanRepository: BacaanRepository,
    private val progresRepository: ProgresRepository,
    private val targetRepository: TargetRepository,
    private val profilRepository: ProfilRepository,
    private val appContext: Context
) : ViewModel() {

    private val _fileList = MutableStateFlow<List<FileLaporanDto>>(emptyList())
    val fileList: StateFlow<List<FileLaporanDto>> = _fileList.asStateFlow()

    private val _selectedFile = MutableStateFlow<FileLaporanDto?>(null)
    val selectedFile: StateFlow<FileLaporanDto?> = _selectedFile.asStateFlow()

    private val _selectedFileContent = MutableStateFlow<String?>(null)
    val selectedFileContent: StateFlow<String?> = _selectedFileContent.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isReading = MutableStateFlow(false)
    val isReading: StateFlow<Boolean> = _isReading.asStateFlow()

    private val _isDeleteDialogOpen = MutableStateFlow(false)
    val isDeleteDialogOpen: StateFlow<Boolean> = _isDeleteDialogOpen.asStateFlow()

    private val _fileToDelete = MutableStateFlow<FileLaporanDto?>(null)
    val fileToDelete: StateFlow<FileLaporanDto?> = _fileToDelete.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadDaftarFile()
    }

    fun resetSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun loadDaftarFile() {
        _fileList.value = LocalFileHelper.getDaftarFileLaporan(appContext)
    }

    fun eksporLaporan() {
        _isExporting.value = true
        viewModelScope.launch {
            try {
                val profil = profilRepository.getProfil().getOrNull()
                val namaUser = profil?.nama ?: "Pengguna BacaLog"

                val listBacaan = bacaanRepository.getBacaanList().getOrDefault(emptyList())
                val listProgres = progresRepository.getProgresList().getOrDefault(emptyList())
                val listTarget = targetRepository.getTargetList().getOrDefault(emptyList())

                val result = LocalFileHelper.exportLaporanTxt(
                    context = appContext,
                    namaPengguna = namaUser,
                    listBacaan = listBacaan,
                    listProgres = listProgres,
                    listTarget = listTarget
                )

                result.onSuccess { fileName ->
                    _snackbarMessage.value = "Berhasil mengekspor berkas $fileName"
                    loadDaftarFile()
                }.onFailure { err ->
                    _snackbarMessage.value = "Gagal mengekspor laporan: ${err.message}"
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Terjadi kesalahan saat mengekspor: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun bacaFile(file: FileLaporanDto) {
        _isReading.value = true
        _selectedFile.value = file
        viewModelScope.launch {
            val result = LocalFileHelper.bacaFileLaporan(appContext, file.fileName)
            result.onSuccess { content ->
                _selectedFileContent.value = content
            }.onFailure { err ->
                _snackbarMessage.value = "Gagal membaca berkas: ${err.message}"
                _selectedFile.value = null
                _selectedFileContent.value = null
            }
            _isReading.value = false
        }
    }

    fun tutupPratinjau() {
        _selectedFile.value = null
        _selectedFileContent.value = null
    }

    fun openDeleteDialog(file: FileLaporanDto) {
        _fileToDelete.value = file
        _isDeleteDialogOpen.value = true
    }

    fun closeDeleteDialog() {
        _isDeleteDialogOpen.value = false
        _fileToDelete.value = null
    }

    fun confirmDelete() {
        val target = _fileToDelete.value ?: return
        val success = LocalFileHelper.hapusFileLaporan(appContext, target.fileName)
        if (success) {
            _snackbarMessage.value = "Berkas ${target.fileName} berhasil dihapus"
            if (_selectedFile.value?.fileName == target.fileName) {
                tutupPratinjau()
            }
            loadDaftarFile()
        } else {
            _snackbarMessage.value = "Gagal menghapus berkas ${target.fileName}"
        }
        closeDeleteDialog()
    }
}
