package com.example.tubespm.ui.screens.activity

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
import androidx.navigation.NavController
import com.example.tubespm.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTryoutScreen(navController : NavController) {
    val belumDikerjakanList = remember { sampleTryoutList() }
    val dalamProsesList = remember { sampleInProgressList() }
    val selesaiList = remember { sampleCompletedList() }

    var selectedTabIndex by remember { mutableStateOf(0) }
    var tabs = listOf("Belum Dikerjakan", "Dalam Proses", "Selesai")

    // State untuk mengontrol dialog detail
    var showDetailDialogFor by remember { mutableStateOf<Tryout?>(null) }

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
        when (selectedTabIndex) {
            0 -> TryoutBelumDikerjakanContent(
                    tryouts = belumDikerjakanList,
                    onCardClick = { tryout ->
                        showDetailDialogFor = tryout
                    }
                )
            1 -> TryoutDalamProsesContent(
                tryouts = dalamProsesList,
                onContinueClick = {
                    navController.navigate("tryout_quiz")
                }
            )
            2 -> TryoutSelesaiContent(selesaiList)
        }
    }

    // Tampilkan dialog jika 'showDetailDialogFor' tidak null
    showDetailDialogFor?.let { tryout ->
        TryoutDetailDialog(
            tryout = tryout,
            onDismiss = { showDetailDialogFor = null },
            onStart = {
                // INI BAGIAN PENTINGNYA
                // 1. Tutup dialog
                showDetailDialogFor = null
                // 2. Lakukan navigasi ke QuizScreen
                navController.navigate("tryout_quiz")
            },
            onCancel = {
                // TODO: Logika untuk membatalkan/menghapus tryout dari daftar
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