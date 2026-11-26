package com.example.bacalog.ui.screens.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bacalog.R
import com.example.bacalog.data.model.FokusDto
import com.example.bacalog.data.model.UserProfilDto
import com.example.bacalog.ui.components.BacaLogBottomBar
import com.example.bacalog.ui.navigation.Screen
import com.example.bacalog.ui.theme.SuccessGreen
import com.example.bacalog.viewmodel.FokusUiState
import com.example.bacalog.viewmodel.ProfilUiState
import com.example.bacalog.viewmodel.ProfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    viewModel: ProfilViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profilUiState by viewModel.profilUiState.collectAsState()
    val fokusUiState by viewModel.fokusUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()

    val isEditProfilOpen by viewModel.isEditProfilOpen.collectAsState()
    val isAddEditFokusOpen by viewModel.isAddEditFokusOpen.collectAsState()
    val fokusBeingEdited by viewModel.fokusBeingEdited.collectAsState()
    val isDeleteFokusOpen by viewModel.isDeleteFokusOpen.collectAsState()
    val fokusToDelete by viewModel.fokusToDelete.collectAsState()
    val isLogoutDialogOpen by viewModel.isLogoutDialogOpen.collectAsState()

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
        viewModel.loadProfil()
        viewModel.loadFokus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_profil_judul),
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
                currentRoute = Screen.Profil.route,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddFokusDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.fab_tambah_fokus)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(dimensionResource(R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_large))
        ) {
            // 1. Kartu Informasi Profil Pengguna
            item {
                when (val state = profilUiState) {
                    is ProfilUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is ProfilUiState.Success -> {
                        ProfilHeaderCard(
                            user = state.profil,
                            onEditClick = { viewModel.openEditProfilDialog() }
                        )
                    }
                    is ProfilUiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                            ) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadProfil() },
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
            }

            // 2. Section Header: Catatan Fokus Belajar (CRUDS 5)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.profil_fokus_section_judul),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.profil_fokus_section_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. SearchBar Catatan Fokus Belajar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text(stringResource(R.string.fokus_search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.desc_clear_search)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Filter Chips Status Fokus
            item {
                val filterOptions = listOf(
                    "Semua" to stringResource(R.string.fokus_status_semua),
                    "Aktif" to stringResource(R.string.fokus_status_aktif),
                    "Selesai" to stringResource(R.string.fokus_status_selesai)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    filterOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == key,
                            onClick = { viewModel.onStatusFilterSelected(key) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // 5. Daftar Catatan Fokus Belajar (CRUDS 5)
            when (val state = fokusUiState) {
                is FokusUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                is FokusUiState.Empty -> {
                    item {
                        val isSearching = searchQuery.trim().isNotEmpty()
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(R.dimen.padding_large)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                            ) {
                                Icon(
                                    imageVector = if (isSearching) Icons.Default.Search else Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Text(
                                    text = if (isSearching) stringResource(R.string.fokus_tidak_ditemukan) else stringResource(R.string.fokus_kosong_judul),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSearching) stringResource(R.string.fokus_tidak_ditemukan_desc) else stringResource(R.string.fokus_kosong_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                is FokusUiState.Success -> {
                    items(state.listFokus, key = { it.id }) { fokus ->
                        FokusCard(
                            fokus = fokus,
                            onToggleStatus = { viewModel.toggleFokusStatus(fokus) },
                            onEditClick = { viewModel.openEditFokusDialog(fokus) },
                            onDeleteClick = { viewModel.openDeleteFokusDialog(fokus) }
                        )
                    }
                }
                is FokusUiState.Error -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                            ) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadFokus() },
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
            }

            // 6. Tombol Logout di Bagian Paling Bawah
            item {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
                OutlinedButton(
                    onClick = { viewModel.openLogoutDialog() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                    Text(
                        text = stringResource(R.string.btn_keluar_akun),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))
            }
        }
    }

    // Dialog 1: Ubah Informasi Profil
    if (isEditProfilOpen) {
        val currentProfil = (profilUiState as? ProfilUiState.Success)?.profil
        EditProfilDialog(
            user = currentProfil,
            errorMessage = dialogError,
            onDismiss = { viewModel.closeEditProfilDialog() },
            onSave = { nama, bio, passLama, passBaru ->
                viewModel.saveProfil(nama, bio, passLama, passBaru)
            }
        )
    }

    // Dialog 2: Tambah / Ubah Catatan Fokus
    if (isAddEditFokusOpen) {
        AddEditFokusDialog(
            fokus = fokusBeingEdited,
            errorMessage = dialogError,
            onDismiss = { viewModel.closeAddEditFokusDialog() },
            onSave = { materi, keterangan, target, status ->
                viewModel.saveFokus(materi, keterangan, target, status)
            }
        )
    }

    // Dialog 3: Konfirmasi Hapus Fokus
    if (isDeleteFokusOpen && fokusToDelete != null) {
        val target = fokusToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteFokusDialog() },
            title = { Text(stringResource(R.string.fokus_hapus_konfirmasi_judul)) },
            text = {
                Text(stringResource(R.string.fokus_hapus_konfirmasi_pesan, target.fokus_materi))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteFokus() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_hapus))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDeleteFokusDialog() }) {
                    Text(stringResource(R.string.btn_batal))
                }
            }
        )
    }

    // Dialog 4: Konfirmasi Logout
    if (isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeLogoutDialog() },
            title = { Text(stringResource(R.string.dialog_logout_judul)) },
            text = { Text(stringResource(R.string.dialog_logout_pesan)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout(onLogoutSuccess) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_keluar))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeLogoutDialog() }) {
                    Text(stringResource(R.string.btn_batal))
                }
            }
        )
    }
}

@Composable
fun ProfilHeaderCard(
    user: UserProfilDto,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_large))
            ) {
                // Avatar Inisial
                val initial = if (user.nama.isNotBlank()) user.nama.trim().take(1).uppercase() else "U"
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = user.nama,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profil_badge_peran),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bio & Motivasi Belajar
            Surface(
                shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profil_bio_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = user.bio_belajar?.ifBlank { null } ?: stringResource(R.string.profil_bio_default),
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tombol Ubah Profil
            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                Text(stringResource(R.string.profil_btn_edit))
            }
        }
    }
}

@Composable
fun FokusCard(
    fokus: FokusDto,
    onToggleStatus: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelesai = fokus.status_fokus.equals("Selesai", ignoreCase = true)
    val statusBg = if (isSelesai) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
    val statusColor = if (isSelesai) SuccessGreen else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            // Header Baris: Chip Status & Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Text(
                        text = fokus.status_fokus,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleStatus,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelesai) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = stringResource(R.string.desc_toggle_fokus),
                            tint = if (isSelesai) SuccessGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.desc_ubah_fokus),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.desc_hapus_fokus),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Judul Materi Fokus
            Text(
                text = fokus.fokus_materi,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textDecoration = if (isSelesai) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isSelesai) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )

            // Keterangan / Rencana
            if (!fokus.keterangan.isNullOrBlank()) {
                Text(
                    text = fokus.keterangan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Target Selesai
            if (!fokus.target_selesai.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.fokus_target_format, fokus.target_selesai),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EditProfilDialog(
    user: UserProfilDto?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (nama: String, bio: String?, passwordLama: String?, passwordBaru: String?) -> Unit
) {
    var nama by remember { mutableStateOf(user?.nama ?: "") }
    var bio by remember { mutableStateOf(user?.bio_belajar ?: "") }
    var passwordLama by remember { mutableStateOf("") }
    var passwordBaru by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.profil_dialog_edit_judul),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                        )
                    }
                }

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text(stringResource(R.string.field_nama)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(stringResource(R.string.profil_bio_label)) },
                    placeholder = { Text(stringResource(R.string.profil_bio_default)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordLama,
                    onValueChange = { passwordLama = it },
                    label = { Text(stringResource(R.string.profil_password_lama_label)) },
                    placeholder = { Text(stringResource(R.string.profil_password_lama_placeholder)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordBaru,
                    onValueChange = { passwordBaru = it },
                    label = { Text(stringResource(R.string.profil_password_baru_label)) },
                    placeholder = { Text(stringResource(R.string.profil_password_baru_placeholder)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nama, bio, passwordLama, passwordBaru) }
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

@Composable
fun AddEditFokusDialog(
    fokus: FokusDto?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (materi: String, keterangan: String?, target: String?, status: String) -> Unit
) {
    var fokusMateri by remember { mutableStateOf(fokus?.fokus_materi ?: "") }
    var keterangan by remember { mutableStateOf(fokus?.keterangan ?: "") }
    var targetSelesai by remember { mutableStateOf(fokus?.target_selesai ?: "") }
    var statusFokus by remember { mutableStateOf(fokus?.status_fokus ?: "Aktif") }

    val isEdit = fokus != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(if (isEdit) R.string.fokus_edit_judul else R.string.fokus_tambah_judul),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                        )
                    }
                }

                OutlinedTextField(
                    value = fokusMateri,
                    onValueChange = { fokusMateri = it },
                    label = { Text(stringResource(R.string.fokus_materi_label)) },
                    placeholder = { Text(stringResource(R.string.fokus_materi_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text(stringResource(R.string.fokus_keterangan_label)) },
                    placeholder = { Text(stringResource(R.string.fokus_keterangan_placeholder)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetSelesai,
                    onValueChange = { targetSelesai = it },
                    label = { Text(stringResource(R.string.fokus_target_label)) },
                    placeholder = { Text(stringResource(R.string.fokus_target_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    listOf("Aktif", "Selesai").forEach { st ->
                        FilterChip(
                            selected = statusFokus == st,
                            onClick = { statusFokus = st },
                            label = { Text(st) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(fokusMateri, keterangan, targetSelesai, statusFokus) }
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
