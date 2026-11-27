package com.example.bacalog.ui.screens.laporan

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bacalog.R
import com.example.bacalog.data.local.FileLaporanDto
import com.example.bacalog.ui.components.BacaLogBottomBar
import com.example.bacalog.ui.navigation.Screen
import com.example.bacalog.viewmodel.LaporanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanLokalScreen(
    viewModel: LaporanViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fileList by viewModel.fileList.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val selectedFileContent by viewModel.selectedFileContent.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val isReading by viewModel.isReading.collectAsState()
    val isDeleteDialogOpen by viewModel.isDeleteDialogOpen.collectAsState()
    val fileToDelete by viewModel.fileToDelete.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.resetSnackbarMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadDaftarFile()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_laporan_judul),
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
                currentRoute = Screen.Laporan.route,
                onNavigate = onNavigate
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = dimensionResource(R.dimen.padding_large),
                top = dimensionResource(R.dimen.padding_large),
                end = dimensionResource(R.dimen.padding_large),
                bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_large))
        ) {
            // 1. Banner Card: Informasi Internal Storage & Tombol Ekspor
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.laporan_banner_judul),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.laporan_banner_subjudul),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.laporan_banner_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Button(
                            onClick = { viewModel.eksporLaporan() },
                            enabled = !isExporting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                                Text(stringResource(R.string.status_mengekspor))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                                Text(
                                    text = stringResource(R.string.laporan_btn_ekspor),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Section Header: Berkas Laporan Tersimpan
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.laporan_section_tersimpan),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${fileList.size} Berkas",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 3. Daftar Berkas Tersimpan
            if (fileList.isEmpty()) {
                item {
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
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = stringResource(R.string.laporan_kosong_judul),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.laporan_kosong_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(fileList, key = { it.fileName }) { file ->
                    FileLaporanCard(
                        file = file,
                        onBacaClick = { viewModel.bacaFile(file) },
                        onDeleteClick = { viewModel.openDeleteDialog(file) }
                    )
                }
            }
        }
    }

    // Dialog 1: Pratinjau Teks Isi Berkas Laporan
    if (selectedFile != null && selectedFileContent != null) {
        LaporanPreviewDialog(
            fileName = selectedFile!!.fileName,
            content = selectedFileContent!!,
            onDismiss = { viewModel.tutupPratinjau() }
        )
    }

    // Dialog 2: Konfirmasi Hapus Berkas
    if (isDeleteDialogOpen && fileToDelete != null) {
        val target = fileToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteDialog() },
            title = { Text(stringResource(R.string.laporan_hapus_konfirmasi_judul)) },
            text = {
                Text(stringResource(R.string.laporan_hapus_konfirmasi_pesan, target.fileName))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_hapus))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDeleteDialog() }) {
                    Text(stringResource(R.string.btn_batal))
                }
            }
        )
    }
}

@Composable
fun FileLaporanCard(
    file: FileLaporanDto,
    onBacaClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ".TXT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${file.formattedSize} • ${file.formattedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBacaClick) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = stringResource(R.string.desc_lihat_laporan),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.desc_hapus_laporan),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun LaporanPreviewDialog(
    fileName: String,
    content: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_large)),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = stringResource(R.string.laporan_dialog_pratinjau_judul),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

                    // Kotak Teks Monospace Isi Laporan
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val verticalScroll = rememberScrollState()
                        val horizontalScroll = rememberScrollState()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(verticalScroll)
                                .horizontalScroll(horizontalScroll)
                                .padding(dimensionResource(R.dimen.padding_medium))
                        ) {
                            Text(
                                text = content,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.laporan_dialog_tutup))
                }
            }
        }
    }
}
