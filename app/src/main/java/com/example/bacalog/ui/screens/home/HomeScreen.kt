package com.example.bacalog.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bacalog.R
import com.example.bacalog.ui.components.BacaLogBottomBar
import com.example.bacalog.ui.components.BacaLogTopBar
import com.example.bacalog.ui.components.MenuCard
import com.example.bacalog.ui.components.StatistikCard
import com.example.bacalog.ui.navigation.Screen
import com.example.bacalog.ui.theme.IndigoPrimary
import com.example.bacalog.ui.theme.IndigoSecondary
import com.example.bacalog.ui.theme.IndigoTertiary
import com.example.bacalog.ui.theme.SuccessGreen
import com.example.bacalog.ui.theme.WarningAmber
import com.example.bacalog.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stat by homeViewModel.statistik.collectAsState()
    val namaPengguna by homeViewModel.namaPengguna.collectAsState()
    val activeTargetSummary by homeViewModel.activeTargetSummary.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.loadStatistik()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BacaLogTopBar(judul = stringResource(R.string.app_name))
        },
        bottomBar = {
            BacaLogBottomBar(
                currentRoute = Screen.Home.route,
                onNavigate = onNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = dimensionResource(R.dimen.padding_medium),
                    top = dimensionResource(R.dimen.padding_medium),
                    end = dimensionResource(R.dimen.padding_medium),
                    bottom = 88.dp
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            // 1. Banner Sambutan Pengguna Personal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(IndigoPrimary, IndigoSecondary)
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_large))
                    ) {
                        val greeting = if (!namaPengguna.isNullOrBlank()) {
                            stringResource(R.string.dashboard_salam_personal, namaPengguna!!)
                        } else {
                            stringResource(R.string.dashboard_salam_pembuka)
                        }
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
                        Text(
                            text = stringResource(R.string.dashboard_salam_sub),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_medium)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                    ) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { homeViewModel.loadStatistik() },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(text = stringResource(R.string.btn_coba_lagi))
                        }
                    }
                }
            }

            // 2. Bagian Ringkasan Statistik Belajar
            Text(
                text = stringResource(R.string.section_statistik_belajar),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Baris 1 Statistik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                StatistikCard(
                    judul = stringResource(R.string.stat_total_bacaan),
                    nilai = "${stat.totalBacaan}",
                    ikon = Icons.AutoMirrored.Filled.MenuBook,
                    warnaAksen = MaterialTheme.colorScheme.primary,
                    warnaLatar = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )
                StatistikCard(
                    judul = stringResource(R.string.stat_sedang_dibaca),
                    nilai = "${stat.sedangDibaca}",
                    ikon = Icons.Default.Timer,
                    warnaAksen = WarningAmber,
                    warnaLatar = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )
            }

            // Baris 2 Statistik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                StatistikCard(
                    judul = stringResource(R.string.stat_selesai),
                    nilai = "${stat.selesaiDibaca}",
                    ikon = Icons.Default.CheckCircle,
                    warnaAksen = SuccessGreen,
                    warnaLatar = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )
                StatistikCard(
                    judul = stringResource(R.string.stat_halaman),
                    nilai = "${stat.totalHalaman}",
                    ikon = Icons.AutoMirrored.Filled.TrendingUp,
                    warnaAksen = IndigoTertiary,
                    warnaLatar = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Banner Target Belajar Aktif (Dinamis & Clickable ke TargetListScreen)
            val cardShape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .clickable { onNavigate(Screen.Target.route) },
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsScore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dashboard_target_banner_judul),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val highlighted = activeTargetSummary.highlightedTarget
                        val targetDesc = if (highlighted != null) {
                            stringResource(
                                R.string.dashboard_target_banner_dinamis,
                                activeTargetSummary.activeCount,
                                highlighted.judulBacaan ?: stringResource(R.string.menu_bacaan_judul),
                                highlighted.capaianTertinggi ?: 0,
                                highlighted.targetHalaman
                            )
                        } else {
                            stringResource(R.string.dashboard_target_banner_kosong)
                        }
                        Text(
                            text = targetDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.menu_target_judul),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. Menu Modul Utama (Seluruh Fitur SRS Terjangkau dari Beranda)
            Text(
                text = stringResource(R.string.section_menu_utama),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            MenuCard(
                judul = stringResource(R.string.menu_kategori_judul),
                deskripsi = stringResource(R.string.menu_kategori_desc),
                ikon = Icons.Default.Folder,
                warnaIkon = MaterialTheme.colorScheme.primary,
                warnaLatarIkon = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onNavigate(Screen.Kategori.route) }
            )

            MenuCard(
                judul = stringResource(R.string.menu_target_judul),
                deskripsi = stringResource(R.string.menu_target_desc),
                ikon = Icons.Default.SportsScore,
                warnaIkon = SuccessGreen,
                warnaLatarIkon = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(Screen.Target.route) }
            )

            MenuCard(
                judul = stringResource(R.string.menu_laporan_judul),
                deskripsi = stringResource(R.string.menu_laporan_desc),
                ikon = Icons.Default.Save,
                warnaIkon = MaterialTheme.colorScheme.primary,
                warnaLatarIkon = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onNavigate(Screen.Laporan.route) }
            )

            MenuCard(
                judul = stringResource(R.string.menu_bacaan_judul),
                deskripsi = stringResource(R.string.menu_bacaan_desc),
                ikon = Icons.AutoMirrored.Filled.MenuBook,
                warnaIkon = IndigoTertiary,
                warnaLatarIkon = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onNavigate(Screen.Bacaan.route) }
            )

            MenuCard(
                judul = stringResource(R.string.menu_progres_judul),
                deskripsi = stringResource(R.string.menu_progres_desc),
                ikon = Icons.AutoMirrored.Filled.Assignment,
                warnaIkon = WarningAmber,
                warnaLatarIkon = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(Screen.Progres.route) }
            )

            MenuCard(
                judul = stringResource(R.string.menu_profil_judul),
                deskripsi = stringResource(R.string.menu_profil_desc),
                ikon = Icons.Default.Person,
                warnaIkon = MaterialTheme.colorScheme.secondary,
                warnaLatarIkon = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(Screen.Profil.route) }
            )
        }
    }
}
