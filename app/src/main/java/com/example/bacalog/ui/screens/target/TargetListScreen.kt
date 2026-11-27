package com.example.bacalog.ui.screens.target

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.FilterChip
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bacalog.R
import com.example.bacalog.data.model.BacaanDto
import com.example.bacalog.data.model.TargetDto
import com.example.bacalog.ui.theme.SuccessGreen
import com.example.bacalog.ui.theme.WarningAmber
import androidx.compose.material.icons.filled.Description
import com.example.bacalog.ui.navigation.Screen
import com.example.bacalog.viewmodel.TargetUiState
import com.example.bacalog.viewmodel.TargetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetListScreen(
    viewModel: TargetViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilterStatus.collectAsState()
    val bacaanList by viewModel.bacaanList.collectAsState()
    val isAddEditDialogOpen by viewModel.isAddEditDialogOpen.collectAsState()
    val targetBeingEdited by viewModel.targetBeingEdited.collectAsState()
    val isDeleteDialogOpen by viewModel.isDeleteDialogOpen.collectAsState()
    val targetToDelete by viewModel.targetToDelete.collectAsState()
    val dialogError by viewModel.dialogError.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.resetSnackbarMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_target_judul),
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
                ),
                actions = {
                    IconButton(onClick = { onNavigate(Screen.Laporan.route) }) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = stringResource(R.string.menu_laporan_judul),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                    contentDescription = stringResource(R.string.fab_tambah_target)
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

            // 1. Kotak Pencarian Target
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                placeholder = { Text(text = stringResource(R.string.target_search_placeholder)) },
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
                shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // 2. Baris Filter Status Target (LazyRow Anti Terpotong)
            val filters = listOf(
                stringResource(R.string.target_status_semua),
                stringResource(R.string.target_status_berjalan),
                stringResource(R.string.target_status_tercapai)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.padding_medium)),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { statusName ->
                    val isSelected = selectedFilter.equals(statusName, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterStatusChanged(statusName) },
                        label = { Text(text = statusName) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
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

            // 3. Konten Daftar Target Berdasarkan State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium))
            ) {
                when (val state = uiState) {
                    is TargetUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    is TargetUiState.Empty -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(dimensionResource(R.dimen.padding_large)),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(R.dimen.padding_large)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsScore,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                                val isSearchingOrFiltering = searchQuery.isNotBlank() || selectedFilter != stringResource(R.string.target_status_semua)
                                Text(
                                    text = if (isSearchingOrFiltering)
                                        stringResource(R.string.target_tidak_ditemukan)
                                    else
                                        stringResource(R.string.target_kosong_judul),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
                                Text(
                                    text = if (isSearchingOrFiltering)
                                        stringResource(R.string.target_tidak_ditemukan_desc)
                                    else
                                        stringResource(R.string.target_kosong_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    is TargetUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimensionResource(R.dimen.padding_medium)),
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
                                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
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
                                        onClick = { viewModel.loadTarget() },
                                        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text(text = stringResource(R.string.btn_coba_lagi))
                                    }
                                }
                            }
                        }
                    }

                    is TargetUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(state.listTarget, key = { it.id }) { target ->
                                TargetItemCard(
                                    target = target,
                                    onEdit = { viewModel.openEditDialog(target) },
                                    onDelete = { viewModel.openDeleteDialog(target) },
                                    onToggleStatus = { viewModel.toggleStatus(target) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah / Edit Target
    if (isAddEditDialogOpen) {
        TargetFormDialog(
            targetToEdit = targetBeingEdited,
            bacaanList = bacaanList,
            errorMessage = dialogError,
            defaultDeadline = viewModel.getDefaultDeadlineString(),
            onDismiss = { viewModel.dismissAddEditDialog() },
            onSave = { bacaanId, targetHalaman, deadline, statusTarget ->
                viewModel.saveTarget(bacaanId, targetHalaman, deadline, statusTarget)
            }
        )
    }

    // Dialog Konfirmasi Hapus Target
    if (isDeleteDialogOpen && targetToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.target_hapus_konfirmasi_judul),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.target_hapus_konfirmasi_pesan,
                        targetToDelete?.judulBacaan ?: "Bahan Bacaan"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteTarget() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(R.string.btn_hapus))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text(text = stringResource(R.string.btn_batal))
                }
            }
        )
    }
}

@Composable
fun TargetItemCard(
    target: TargetDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTercapai = target.statusTarget.equals("Tercapai", ignoreCase = true)
    val capaian = target.capaianTertinggi ?: 0
    val progressRatio = if (target.targetHalaman > 0) {
        (capaian.toFloat() / target.targetHalaman.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            // Baris Atas: Kategori Topik + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = target.namaKategori ?: "Kategori Belajar",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                    color = if (isTercapai) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isTercapai) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = if (isTercapai) SuccessGreen else WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = target.statusTarget,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isTercapai) SuccessGreen else WarningAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // Judul Bahan Bacaan
            Text(
                text = target.judulBacaan ?: "Bahan Bacaan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_small)))

            // Target vs Capaian Teks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.target_capaian_format, target.targetHalaman, capaian),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progressRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isTercapai) SuccessGreen else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Progress Bar (Animated)
            val animatedTargetProgress by animateFloatAsState(
                targetValue = progressRatio,
                label = "target_progress_anim"
            )
            LinearProgressIndicator(
                progress = { animatedTargetProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (isTercapai) SuccessGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // Baris Bawah: Tenggat Waktu & Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Deadline
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.target_deadline_format, target.deadline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tombol Aksi
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleStatus,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isTercapai) Icons.Default.PendingActions else Icons.Default.CheckCircle,
                            contentDescription = "Ubah Status",
                            tint = if (isTercapai) WarningAmber else SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.desc_ubah_target),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.desc_hapus_target),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TargetFormDialog(
    targetToEdit: TargetDto?,
    bacaanList: List<BacaanDto>,
    errorMessage: String?,
    defaultDeadline: String,
    onDismiss: () -> Unit,
    onSave: (bacaanId: Int, targetHalaman: Int, deadline: String, statusTarget: String) -> Unit
) {
    var selectedBacaanId by remember {
        mutableStateOf(targetToEdit?.bacaanId ?: bacaanList.firstOrNull()?.id ?: 0)
    }
    var targetHalamanText by remember {
        mutableStateOf(targetToEdit?.targetHalaman?.toString() ?: "")
    }
    var deadlineText by remember {
        mutableStateOf(targetToEdit?.deadline ?: defaultDeadline)
    }
    var selectedStatus by remember {
        mutableStateOf(targetToEdit?.statusTarget ?: "Sedang Berjalan")
    }

    var isBacaanDropdownExpanded by remember { mutableStateOf(false) }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }

    var localValidationError by remember { mutableStateOf<String?>(null) }

    val isEdit = targetToEdit != null
    val selectedBacaanTitle = bacaanList.find { it.id == selectedBacaanId }?.judul
        ?: stringResource(R.string.target_bacaan_pilih)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (isEdit) stringResource(R.string.target_edit_judul) else stringResource(R.string.target_tambah_judul),
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
                // Dropdown Pilihan Bahan Bacaan
                Text(
                    text = stringResource(R.string.target_bacaan_label),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isBacaanDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBacaanTitle,
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
                        expanded = isBacaanDropdownExpanded,
                        onDismissRequest = { isBacaanDropdownExpanded = false }
                    ) {
                        bacaanList.forEach { bacaan ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = bacaan.judul,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${bacaan.namaKategori ?: "Kategori"} • ${bacaan.totalHalaman} Hal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    selectedBacaanId = bacaan.id
                                    if (targetHalamanText.isBlank()) {
                                        targetHalamanText = bacaan.totalHalaman.toString()
                                    }
                                    isBacaanDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Input Target Capaian Halaman
                OutlinedTextField(
                    value = targetHalamanText,
                    onValueChange = {
                        targetHalamanText = it.filter { char -> char.isDigit() }
                        localValidationError = null
                    },
                    label = { Text(text = stringResource(R.string.target_halaman_label)) },
                    placeholder = { Text(text = stringResource(R.string.target_halaman_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                    singleLine = true
                )

                // Input Tenggat Waktu / Deadline
                OutlinedTextField(
                    value = deadlineText,
                    onValueChange = {
                        deadlineText = it
                        localValidationError = null
                    },
                    label = { Text(text = stringResource(R.string.target_deadline_label)) },
                    placeholder = { Text(text = stringResource(R.string.target_deadline_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius)),
                    singleLine = true
                )

                // Dropdown Status Target
                Text(
                    text = stringResource(R.string.target_status_label),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isStatusDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.input_corner_radius))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedStatus)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isStatusDropdownExpanded,
                        onDismissRequest = { isStatusDropdownExpanded = false }
                    ) {
                        listOf("Sedang Berjalan", "Tercapai").forEach { statusOption ->
                            DropdownMenuItem(
                                text = { Text(text = statusOption) },
                                onClick = {
                                    selectedStatus = statusOption
                                    isStatusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Pesan Error Validasi atau Server
                val activeError = localValidationError ?: errorMessage
                if (activeError != null) {
                    Text(
                        text = activeError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedBacaanId <= 0) {
                        localValidationError = "Pilih bahan bacaan terlebih dahulu"
                        return@Button
                    }
                    val hal = targetHalamanText.toIntOrNull()
                    if (hal == null || hal <= 0) {
                        localValidationError = "Target halaman harus berupa angka lebih dari 0"
                        return@Button
                    }
                    if (deadlineText.isBlank()) {
                        localValidationError = "Tenggat waktu / deadline tidak boleh kosong"
                        return@Button
                    }
                    if (!Regex("""^\d{4}-\d{2}-\d{2}$""").matches(deadlineText.trim())) {
                        localValidationError = "Format deadline harus YYYY-MM-DD (contoh: 2026-11-30)"
                        return@Button
                    }

                    localValidationError = null
                    onSave(selectedBacaanId, hal, deadlineText.trim(), selectedStatus)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = stringResource(R.string.btn_simpan))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.btn_batal))
            }
        }
    )
}
