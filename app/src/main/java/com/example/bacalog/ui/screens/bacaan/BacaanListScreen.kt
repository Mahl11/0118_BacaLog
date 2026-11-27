package com.example.bacalog.ui.screens.bacaan

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bacalog.R
import com.example.bacalog.data.model.BacaanDto
import com.example.bacalog.data.model.KategoriDto
import com.example.bacalog.ui.components.BacaLogBottomBar
import com.example.bacalog.ui.navigation.Screen
import com.example.bacalog.ui.theme.SuccessGreen
import com.example.bacalog.ui.theme.WarningAmber
import com.example.bacalog.viewmodel.BacaanUiState
import com.example.bacalog.viewmodel.BacaanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacaanListScreen(
    viewModel: BacaanViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()
    val kategoriList by viewModel.kategoriList.collectAsState()
    val isAddEditDialogOpen by viewModel.isAddEditDialogOpen.collectAsState()
    val bacaanBeingEdited by viewModel.bacaanBeingEdited.collectAsState()
    val isDeleteDialogOpen by viewModel.isDeleteDialogOpen.collectAsState()
    val bacaanToDelete by viewModel.bacaanToDelete.collectAsState()
    val dialogError by viewModel.dialogError.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.resetSnackbarMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadKategori()
        viewModel.loadBacaan()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_bacaan_judul),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            BacaLogBottomBar(
                currentRoute = Screen.Bacaan.route,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.fab_tambah_bacaan)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // 1. Search Bar Pencarian Judul / Mata Kuliah / Penulis
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text(stringResource(R.string.bacaan_search_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.desc_clear_search)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // 2. Filter Status Chips LazyRow (Seragam Soft Blue Container + Deep Indigo, Anti Terpotong)
            val filterOptions = listOf(
                "Semua" to stringResource(R.string.bacaan_status_semua),
                "Belum Dibaca" to stringResource(R.string.bacaan_status_belum),
                "Sedang Dibaca" to stringResource(R.string.bacaan_status_sedang),
                "Selesai" to stringResource(R.string.bacaan_status_selesai)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.padding_medium)),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { (key, label) ->
                    val isSelected = selectedStatusFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onStatusFilterSelected(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // 3. Konten Sesuai UI State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
            ) {
                when (val state = uiState) {
                    is BacaanUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is BacaanUiState.Empty -> {
                        val isSearchingOrFiltering = searchQuery.trim().isNotEmpty() || selectedStatusFilter != "Semua"
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large))
                            ) {
                                Icon(
                                    imageVector = if (isSearchingOrFiltering) Icons.Default.Search else Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = if (isSearchingOrFiltering) stringResource(R.string.bacaan_tidak_ditemukan) else stringResource(R.string.bacaan_kosong_judul),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSearchingOrFiltering) stringResource(R.string.bacaan_tidak_ditemukan_desc) else stringResource(R.string.bacaan_kosong_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    is BacaanUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                                ) {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { viewModel.loadBacaan() },
                                        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text(stringResource(R.string.btn_coba_lagi))
                                    }
                                }
                            }
                        }
                    }
                    is BacaanUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                        ) {
                            items(state.listBacaan, key = { it.id }) { bacaan ->
                                BacaanItemCard(
                                    bacaan = bacaan,
                                    onEditClick = { viewModel.openEditDialog(bacaan) },
                                    onDeleteClick = { viewModel.openDeleteDialog(bacaan) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Dialog Tambah / Ubah Bahan Bacaan
    if (isAddEditDialogOpen) {
        AddEditBacaanDialog(
            bacaan = bacaanBeingEdited,
            kategoriList = kategoriList,
            errorMessage = dialogError,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { katId, judul, penulis, hal, status ->
                viewModel.saveBacaan(katId, judul, penulis, hal, status)
            }
        )
    }

    // 5. Dialog Konfirmasi Hapus Bahan Bacaan
    if (isDeleteDialogOpen && bacaanToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteDialog() },
            title = {
                Text(
                    text = stringResource(R.string.bacaan_hapus_konfirmasi_judul),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.bacaan_hapus_konfirmasi_pesan,
                        bacaanToDelete!!.judul
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_hapus))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.closeDeleteDialog() }) {
                    Text(stringResource(R.string.btn_batal))
                }
            }
        )
    }
}

@Composable
private fun BacaanItemCard(
    bacaan: BacaanDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.card_elevation)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))
        ) {
            // Header Baris: Badge Kategori & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Kategori
                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = bacaan.namaKategori.ifBlank { stringResource(R.string.bacaan_kategori_label) },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badge Status Baca
                val (statusBg, statusFg) = when (bacaan.statusBaca) {
                    "Selesai" -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
                    "Sedang Dibaca" -> WarningAmber.copy(alpha = 0.15f) to WarningAmber
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    color = statusBg
                ) {
                    Text(
                        text = bacaan.statusBaca,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Judul Buku / Slide
            Text(
                text = bacaan.judul,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Penulis / Sumber
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                )
                Text(
                    text = bacaan.penulisSumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Indikator Progres Halaman (Animated)
            val progressPercent = if (bacaan.totalHalaman > 0) {
                (bacaan.halamanTerakhir.toFloat() / bacaan.totalHalaman.toFloat()).coerceIn(0f, 1f)
            } else 0f
            val animatedProgress by animateFloatAsState(
                targetValue = progressPercent,
                label = "bacaan_progress_anim"
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_small)))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            // Baris Bawah: Info Progres & Tombol Aksi (Ubah & Hapus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.bacaan_progres_halaman,
                        bacaan.halamanTerakhir,
                        bacaan.totalHalaman
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.desc_ubah_bacaan),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.desc_hapus_bacaan),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditBacaanDialog(
    bacaan: BacaanDto?,
    kategoriList: List<KategoriDto>,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Int?, String, String, String, String) -> Unit
) {
    var selectedKategoriId by remember(bacaan) {
        mutableStateOf<Int?>(bacaan?.kategoriId ?: kategoriList.firstOrNull()?.id)
    }
    var judulInput by remember(bacaan) { mutableStateOf(bacaan?.judul ?: "") }
    var penulisInput by remember(bacaan) { mutableStateOf(bacaan?.penulisSumber ?: "") }
    var totalHalamanInput by remember(bacaan) {
        mutableStateOf(if (bacaan != null && bacaan.totalHalaman > 0) bacaan.totalHalaman.toString() else "")
    }
    var selectedStatus by remember(bacaan) {
        mutableStateOf(bacaan?.statusBaca ?: "Belum Dibaca")
    }

    var isKategoriExpanded by remember { mutableStateOf(false) }
    var isStatusExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf("Belum Dibaca", "Sedang Dibaca", "Selesai")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (bacaan == null) {
                    stringResource(R.string.bacaan_tambah_judul)
                } else {
                    stringResource(R.string.bacaan_edit_judul)
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                // 1. Dropdown Pilih Kategori Topik
                Box(modifier = Modifier.fillMaxWidth()) {
                    val kategoriSelectedName = kategoriList.find { it.id == selectedKategoriId }?.namaTopik
                        ?: stringResource(R.string.bacaan_kategori_pilih)

                    OutlinedTextField(
                        value = kategoriSelectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.bacaan_kategori_label)) },
                        trailingIcon = {
                            IconButton(onClick = { isKategoriExpanded = !isKategoriExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = isKategoriExpanded,
                        onDismissRequest = { isKategoriExpanded = false }
                    ) {
                        kategoriList.forEach { kategori ->
                            DropdownMenuItem(
                                text = { Text(kategori.namaTopik) },
                                onClick = {
                                    selectedKategoriId = kategori.id
                                    isKategoriExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. Input Judul Bahan Bacaan
                OutlinedTextField(
                    value = judulInput,
                    onValueChange = { judulInput = it },
                    label = { Text(stringResource(R.string.bacaan_judul_label)) },
                    placeholder = { Text(stringResource(R.string.bacaan_judul_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Input Penulis / Sumber
                OutlinedTextField(
                    value = penulisInput,
                    onValueChange = { penulisInput = it },
                    label = { Text(stringResource(R.string.bacaan_penulis_label)) },
                    placeholder = { Text(stringResource(R.string.bacaan_penulis_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Input Total Halaman / Slide
                OutlinedTextField(
                    value = totalHalamanInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            totalHalamanInput = input
                        }
                    },
                    label = { Text(stringResource(R.string.bacaan_halaman_label)) },
                    placeholder = { Text(stringResource(R.string.bacaan_halaman_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Dropdown Status Baca
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.bacaan_status_label)) },
                        trailingIcon = {
                            IconButton(onClick = { isStatusExpanded = !isStatusExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = isStatusExpanded,
                        onDismissRequest = { isStatusExpanded = false }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    selectedStatus = status
                                    isStatusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Banner Error jika ada validasi yang gagal
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedKategoriId, judulInput, penulisInput, totalHalamanInput, selectedStatus)
                }
            ) {
                Text(stringResource(R.string.btn_simpan))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_batal))
            }
        }
    )
}
