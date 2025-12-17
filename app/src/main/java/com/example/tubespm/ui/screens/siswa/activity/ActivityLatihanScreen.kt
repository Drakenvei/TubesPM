package com.example.tubespm.ui.screens.siswa.activity

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tubespm.data.model.* // Pastikan semua model terimport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLatihanScreen(
    navController: NavController,
    viewModel: ActivityLatihanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Belum Dikerjakan", "Dalam Proses", "Selesai")

    var showDetailDialogFor by remember { mutableStateOf<ActivityLatihanDetail?>(null) }

    // --- STATE BARU UNTUK KONFIRMASI PEMBATALAN LATIHAN ---
    var latihanToConfirmCancel by remember { mutableStateOf<ActivityLatihanDetail?>(null) }

    val context = LocalContext.current

    // --- LOGIKA PENERIMA PESAN (Tidak Berubah) ---
    val resultMessageState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("submit_message")
        ?.observeAsState()

    val resultMessage = resultMessageState?.value

    LaunchedEffect(resultMessage) {
        resultMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("submit_message")
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Daftar Latihan",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = {navController.popBackStack()}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE61C5D)
            )
        )

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
                    selectedContentColor = Color.Black,
                    unselectedContentColor = Color.Gray,
                    text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                )
            }
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            when (selectedTabIndex) {
                0 -> LatihanBelumDikerjakanContent(
                    latihanList = uiState.notStarted,
                    onCardClick = { activityDetail ->
                        showDetailDialogFor = activityDetail
                    }
                )
                1 -> LatihanDalamProsesContent(
                    latihanList = uiState.inProgress,
                    onContinueClick = { activityDetail ->
                        navController.navigate("latihan_quiz/${activityDetail.userActivity.id}")
                    }
                )
                2 -> LatihanSelesaiContent(
                    latihanList = uiState.completed,
                    onResultClick = { activityDetail ->
                        navController.navigate("pembahasan/${activityDetail.userActivity.id}")
                    }
                )
            }
        }
    }

    // --- 1. Dialog Detail (Trigger Pembatalan Tahap 1) ---
    showDetailDialogFor?.let { activityDetail ->
        LatihanDetailDialog(
            latihan = activityDetail.latihanSoal,
            onDismiss = { showDetailDialogFor = null },
            onStart = {
                showDetailDialogFor = null
                navController.navigate("latihan_quiz/${activityDetail.userActivity.id}")
            },
            onCancel = {
                // Pindah ke state konfirmasi
                latihanToConfirmCancel = activityDetail
                showDetailDialogFor = null // Tutup dialog detail
            }
        )
    }

    // --- 2. Dialog KONFIRMASI Pembatalan (Trigger Pembatalan Tahap 2) ---
    latihanToConfirmCancel?.let { activityDetail ->
        AlertDialog(
            onDismissRequest = { latihanToConfirmCancel = null },
            title = {
                Text(text = "Batalkan Latihan?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Apakah Anda yakin ingin membatalkan pengambilan latihan '${activityDetail.latihanSoal.title}' ini? Aksi ini tidak dapat diurungkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // AKSI PENTING: Panggil ViewModel untuk menghapus data
                        viewModel.cancelLatihan(activityDetail.userActivity.id)

                        // Umpan balik visual
                        Toast.makeText(context, "Latihan '${activityDetail.latihanSoal.title}' berhasil dibatalkan.", Toast.LENGTH_SHORT).show()

                        latihanToConfirmCancel = null // Tutup dialog konfirmasi
                        // ViewModel diharapkan akan me-refresh uiState.notStarted setelah aksi
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE61C5D)) // Merah
                ) {
                    Text("Ya, Batalkan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { latihanToConfirmCancel = null }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Component yang dipakai (Tidak Berubah)
@Composable
fun SubtestTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            color = Color(0xFFE61C5D),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit){
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}