package com.example.bacalog.ui.screens.progres

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bacalog.R
import com.example.bacalog.data.model.BacaanDto
import com.example.bacalog.data.model.ProgresDto
import com.example.bacalog.ui.components.BacaLogBottomBar
import com.example.bacalog.ui.navigation.Screen
import com.example.bacalog.ui.theme.IndigoContainer
import com.example.bacalog.ui.theme.SlateCard
import com.example.bacalog.ui.theme.SuccessGreen
import com.example.bacalog.ui.theme.WarningAmber
import com.example.bacalog.viewmodel.ProgresUiState
import com.example.bacalog.viewmodel.ProgresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgresListScreen(
    viewModel: ProgresViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val bacaanList by viewModel.bacaanList.collectAsState()
    val isAddEditDialogOpen by viewModel.isAddEditDialogOpen.collectAsState()
    val progresBeingEdited by viewModel.progresBeingEdited.collectAsState()
    val isDeleteDialogOpen by viewModel.isDeleteDialogOpen.collectAsState()
    val progresToDelete by viewModel.progresToDelete.collectAsState()
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
        viewModel.loadBacaan()
        viewModel.loadProgres()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_progres_judul),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_kembali)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            BacaLogBottomBar(
                currentRoute = Screen.Progres.route,
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
                    contentDescription = stringResource(R.string.fab_tambah_progres)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Kolom Pencarian Progres
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(R.dimen.padding_medium),
                        vertical = dimensionResource(R.dimen.padding_small)
                    ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.progres_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
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
                shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
            )

            // Konten Berdasarkan State UI
            when (val state = uiState) {
                is ProgresUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is ProgresUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_large)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    stringResource(R.string.progres_kosong_judul)
                                } else {
                                    stringResource(R.string.progres_tidak_ditemukan)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    stringResource(R.string.progres_kosong_desc)
                                } else {
                                    "Coba cari dengan kata kunci materi atau tanggal lain."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is ProgresUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_large)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
                        ) {
                            Column(
                                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
                            ) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadProgres() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(text = stringResource(R.string.btn_coba_lagi))
                                }
                            }
                        }
                    }
                }

                is ProgresUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = dimensionResource(R.dimen.padding_medium),
                            end = dimensionResource(R.dimen.padding_medium),
                            top = dimensionResource(R.dimen.padding_small),
                            bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
                    ) {
                        items(
                            items = state.listProgres,
                            key = { it.id }
                        ) { item ->
                            ProgresItemCard(
                                progres = item,
                                onEditClick = { viewModel.openEditDialog(item) },
                                onDeleteClick = { viewModel.openDeleteDialog(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah / Edit Catatan Progres
    if (isAddEditDialogOpen) {
        AddEditProgresDialog(
            progres = progresBeingEdited,
            bacaanList = bacaanList,
            errorMessage = dialogError,
            todayDate = viewModel.getTodayDateString(),
            onDismiss = { viewModel.closeAddEditDialog() },
            onConfirm = { bacaanId, tanggal, halaman, ringkasan, target ->
                viewModel.saveProgres(bacaanId, tanggal, halaman, ringkasan, target)
            }
        )
    }

    // Dialog Konfirmasi Hapus
    if (isDeleteDialogOpen && progresToDelete != null) {
        val target = progresToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteDialog() },
            title = {
                Text(
                    text = stringResource(R.string.progres_hapus_konfirmasi_judul),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.progres_hapus_konfirmasi_pesan,
                        target.tanggalBaca
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
                    Text(text = stringResource(R.string.btn_hapus))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.closeDeleteDialog() }) {
                    Text(text = stringResource(R.string.btn_batal))
                }
            }
        )
    }
}

@Composable
fun ProgresItemCard(
    progres: ProgresDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            // Baris Atas: Kategori Badge & Tanggal Sesi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (progres.namaKategori.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = IndigoContainer
                    ) {
                        Text(
                            text = progres.namaKategori,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Tanggal Sesi Belajar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = progres.tanggalBaca,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // Judul Bahan Bacaan
            Text(
                text = progres.judulBacaan.ifBlank { "Bahan Bacaan" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // Metrik Pencapaian: Halaman Dicapai & Target Harian
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SuccessGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(R.string.progres_badge_halaman, progres.halamanDicapai),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WarningAmber.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(R.string.progres_target_badge, progres.targetHarian),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // Ringkasan / Intisari Materi dalam Box abu-abu halus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SlateCard,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(dimensionResource(R.dimen.padding_small))
            ) {
                Text(
                    text = progres.ringkasanMateri,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // Tombol Aksi: Edit & Hapus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.desc_ubah_progres),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.desc_hapus_progres),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProgresDialog(
    progres: ProgresDto?,
    bacaanList: List<BacaanDto>,
    errorMessage: String?,
    todayDate: String,
    onDismiss: () -> Unit,
    onConfirm: (bacaanId: Int?, tanggal: String, halaman: String, ringkasan: String, target: String) -> Unit
) {
    val isEdit = progres != null

    var selectedBacaanId by remember {
        mutableStateOf(progres?.bacaanId ?: bacaanList.firstOrNull()?.id)
    }
    var tanggalBaca by remember {
        mutableStateOf(progres?.tanggalBaca ?: todayDate)
    }
    var halamanDicapai by remember {
        mutableStateOf(progres?.halamanDicapai?.takeIf { it > 0 }?.toString() ?: "")
    }
    var targetHarian by remember {
        mutableStateOf(progres?.targetHarian?.takeIf { it > 0 }?.toString() ?: "")
    }
    var ringkasanMateri by remember {
        mutableStateOf(progres?.ringkasanMateri ?: "")
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedBacaanTitle = bacaanList.firstOrNull { it.id == selectedBacaanId }?.judul
        ?: stringResource(R.string.progres_bacaan_pilih)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    if (isEdit) R.string.progres_edit_judul else R.string.progres_tambah_judul
                ),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                // Pesan Error jika validasi gagal
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Dropdown Pemilihan Bahan Bacaan
                Text(
                    text = stringResource(R.string.progres_bacaan_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBacaanTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        if (bacaanList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.bacaan_kosong_judul)) },
                                onClick = { dropdownExpanded = false }
                            )
                        } else {
                            bacaanList.forEach { bacaan ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = bacaan.judul,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (bacaan.namaKategori.isNotBlank()) {
                                                Text(
                                                    text = bacaan.namaKategori,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedBacaanId = bacaan.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Tanggal Sesi Belajar
                OutlinedTextField(
                    value = tanggalBaca,
                    onValueChange = { tanggalBaca = it },
                    label = { Text(stringResource(R.string.progres_tanggal_label)) },
                    placeholder = { Text(stringResource(R.string.progres_tanggal_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Row: Halaman Dicapai & Target Harian
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    OutlinedTextField(
                        value = halamanDicapai,
                        onValueChange = { halamanDicapai = it },
                        label = { Text(stringResource(R.string.progres_halaman_label)) },
                        placeholder = { Text(stringResource(R.string.progres_halaman_placeholder)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = targetHarian,
                        onValueChange = { targetHarian = it },
                        label = { Text(stringResource(R.string.progres_target_label)) },
                        placeholder = { Text(stringResource(R.string.progres_target_placeholder)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Ringkasan / Intisari Materi
                OutlinedTextField(
                    value = ringkasanMateri,
                    onValueChange = { ringkasanMateri = it },
                    label = { Text(stringResource(R.string.progres_ringkasan_label)) },
                    placeholder = { Text(stringResource(R.string.progres_ringkasan_placeholder)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedBacaanId,
                        tanggalBaca,
                        halamanDicapai,
                        ringkasanMateri,
                        targetHarian
                    )
                }
            ) {
                Text(text = stringResource(R.string.btn_simpan))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.btn_batal))
            }
        }
    )
}
