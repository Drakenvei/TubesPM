package com.example.tubespm.ui.screens.siswa.activity.tryout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTryoutScreen(
    navController : NavController,
    viewModel: ActivityViewModel = hiltViewModel() // inject viewmodel
) {
    // AMBIL STATE DARI VIEWMODEL
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var tabs = listOf("Belum Dikerjakan", "Dalam Proses", "Selesai")

    // State untuk mengontrol dialog detail
    var showDetailDialogFor by remember { mutableStateOf<ActivityTryoutDetail?>(null) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Daftar Tryout",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            navigationIcon = {
                // Tombol kembali ditambahkan di sini
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE61C5D) // Mengatur warna background
            )
        )
        // --- AKHIR PERUBAHAN ---

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            indicator = { TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(it[selectedTabIndex]), height = 3.dp, color = Color.Black)}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    selectedContentColor = Color.Black,     // 🟢 warna teks saat aktif
                    unselectedContentColor = Color.Gray,
                    text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                )
            }
        }

        // Konten berdasarkan state
        if (uiState.isLoading){
            Box (
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null){
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            // Tampilkan konten tab berdasarkan uiState
            when (selectedTabIndex) {
                0 -> TryoutBelumDikerjakanContent(
                    activities = uiState.notStarted, //Gunakan data asli
                    onCardClick = { activityDetail ->
                        showDetailDialogFor = activityDetail //Tampilkan dialog
                    }
                )
                1 -> TryoutDalamProsesContent(
                    activities = uiState.inProgress,
                    onContinueClick = { activityDetail ->
                        navController.navigate("tryout_quiz/${activityDetail.userActivity.id}")
                    }
                )
                2 -> TryoutSelesaiContent(
                    activities = uiState.completed,
                    onResultClick = { activityDetail ->
                        // TODO: Navigasi ke halaman hasil/pembahasan
                        // navController.navigate("tryout_result/${activityDetail.userActivity.id}")
                    }
                )
            }
        }

    }

    // Tampilkan dialog jika 'showDetailDialogFor' tidak null
    showDetailDialogFor?.let { activityDetail ->
        TryoutDetailDialog(
            tryout = activityDetail.tryout, // <-- Kirim data Tryout (untuk UI)
            onDismiss = { showDetailDialogFor = null },
            onStart = {
                showDetailDialogFor = null
                // Kirim ID aktivitas unik (dari UserActivity) ke halaman kuis
                navController.navigate("tryout_quiz/${activityDetail.userActivity.id}")
            },
            onCancel = {
                // --- FUNGSI BARU: Panggil ViewModel untuk membatalkan ---
                viewModel.cancelTryout(activityDetail.userActivity.id)
                showDetailDialogFor = null
            }
        )
    }
}

// InfoRow adalah component
@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}