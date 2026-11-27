package com.example.bacalog.ui.screens.kategori

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bacalog.R
import com.example.bacalog.data.model.KategoriDto
import com.example.bacalog.viewmodel.KategoriUiState
import com.example.bacalog.viewmodel.KategoriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KategoriListScreen(
    viewModel: KategoriViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAddEditDialogOpen by viewModel.isAddEditDialogOpen.collectAsState()
    val kategoriBeingEdited by viewModel.kategoriBeingEdited.collectAsState()
    val isDeleteDialogOpen by viewModel.isDeleteDialogOpen.collectAsState()
    val kategoriToDelete by viewModel.kategoriToDelete.collectAsState()
    val dialogError by viewModel.dialogError.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.resetSnackbarMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_kategori_judul),
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.fab_tambah_kategori)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.padding_medium))
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // 1. Search Bar Pencarian Kategori
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text(stringResource(R.string.kategori_search_placeholder)) },
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

            // 2. Konten Sesuai UI State
            when (val state = uiState) {
                is KategoriUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is KategoriUiState.Empty -> {
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
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) stringResource(R.string.kategori_tidak_ditemukan) else stringResource(R.string.kategori_kosong_judul),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) stringResource(R.string.kategori_tidak_ditemukan_desc) else stringResource(R.string.kategori_kosong_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is KategoriUiState.Error -> {
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
                                    onClick = { viewModel.loadKategori() },
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
                is KategoriUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                    ) {
                        items(state.listKategori, key = { it.id }) { kategori ->
                            KategoriItemCard(
                                kategori = kategori,
                                onEditClick = { viewModel.openEditDialog(kategori) },
                                onDeleteClick = { viewModel.openDeleteDialog(kategori) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 3. Dialog Tambah / Ubah Kategori
    if (isAddEditDialogOpen) {
        AddEditKategoriDialog(
            kategori = kategoriBeingEdited,
            errorMessage = dialogError,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { nama, deskripsi ->
                viewModel.saveKategori(nama, deskripsi)
            }
        )
    }

    // 4. Dialog Konfirmasi Hapus Kategori
    if (isDeleteDialogOpen && kategoriToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteDialog() },
            title = {
                Text(
                    text = stringResource(R.string.kategori_hapus_konfirmasi_judul),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.kategori_hapus_konfirmasi_pesan,
                        kategoriToDelete!!.namaTopik
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
private fun KategoriItemCard(
    kategori: KategoriDto,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Folder dengan latar rounded
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.menu_icon_box_size))
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                )
            }

            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))

            // Detail Kategori
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))
            ) {
                Text(
                    text = kategori.namaTopik,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!kategori.deskripsi.isNullOrBlank()) {
                    Text(
                        text = kategori.deskripsi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge Total Bahan Bacaan
                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.total_bacaan_format, kategori.totalBacaan),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Tombol Edit dan Hapus
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.desc_ubah_kategori),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.desc_hapus_kategori),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddEditKategoriDialog(
    kategori: KategoriDto?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var namaInput by remember(kategori) { mutableStateOf(kategori?.namaTopik ?: "") }
    var deskripsiInput by remember(kategori) { mutableStateOf(kategori?.deskripsi ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (kategori == null) {
                    stringResource(R.string.kategori_tambah_judul)
                } else {
                    stringResource(R.string.kategori_edit_judul)
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                OutlinedTextField(
                    value = namaInput,
                    onValueChange = { namaInput = it },
                    label = { Text(stringResource(R.string.kategori_nama_label)) },
                    placeholder = { Text(stringResource(R.string.kategori_nama_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsiInput,
                    onValueChange = { deskripsiInput = it },
                    label = { Text(stringResource(R.string.kategori_deskripsi_label)) },
                    placeholder = { Text(stringResource(R.string.kategori_deskripsi_placeholder)) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                    modifier = Modifier.fillMaxWidth()
                )

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
                onClick = { onSave(namaInput, deskripsiInput) }
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
