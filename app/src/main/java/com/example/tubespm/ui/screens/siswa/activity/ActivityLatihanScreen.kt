package com.example.tubespm.ui.screens.siswa.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.tubespm.data.model.*
import org.w3c.dom.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLatihanScreen(navController: NavController) {
    val belumDikerjakanList = remember { sampleLatihanList() }
    val dalamProsesList = remember { sampleLatihanInProgressList() }
    val selesaiList = remember { sampleLatihanCompletedList() }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Belum Dikerjakan", "Dalam Proses", "Selesai")

    var showDetailDialogFor by remember { mutableStateOf<LatihanSoal?>(null) }

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
                    selectedContentColor = Color.Black,     // 🟢 warna teks saat aktif
                    unselectedContentColor = Color.Gray,
                    text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                )
            }
        }
        when (selectedTabIndex) {
            0 -> LatihanBelumDikerjakanContent(belumDikerjakanList) { latihan ->
                showDetailDialogFor = latihan
            }
            1 -> LatihanDalamProsesContent(
                latihanList = dalamProsesList,
                onContinueClick = {
                    navController.navigate("latihan_quiz")
                }
            )
            2 -> LatihanSelesaiContent(
                latihanList = selesaiList,
                navController = navController  // Tambahkan parameter ini
            )
        }
    }

    // Dialog Detail
    showDetailDialogFor?.let { latihan ->
        LatihanDetailDialog(
            latihan = latihan,
            onDismiss = { showDetailDialogFor = null },
            onStart = {
                // INI BAGIAN PENTINGNYA
                // 1. Tutup dialog
                showDetailDialogFor = null
                // 2. Lakukan navigasi ke QuizScreen
                navController.navigate("latihan_quiz")
            },
            onCancel = { showDetailDialogFor = null }
        )
    }
}

// Component yang dipakai
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