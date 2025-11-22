package com.example.tubespm.ui.screens.siswa.analisis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalisisScoreScreen(
    navController: NavController,
    viewModel: AnalisisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                            text = "Analisis Skor",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Spacer untuk menjaga judul tetap di tengah
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Skor Besar
                item {
                    ScoreSummaryCard(
                        totalScore = uiState.totalScore,
                        correct = uiState.correctCount,
                        totalQ = uiState.totalQuestions
                    )
                }

                item {
                    Text(
                        "Rincian Subtest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 2. List Skor per Subtest
                items(uiState.scoreDetails) { detail ->
                    SubtestScoreRow(detail)
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {navController.navigate("pembahasan/${uiState.activityId}")},
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lihat Pembahasan Soal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreSummaryCard(totalScore: Int, correct: Int, totalQ: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D)), // Warna Pink Apps
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SKOR UTBK ANDA",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = "$totalScore",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Jawaban Benar: $correct / $totalQ",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SubtestScoreRow(detail: SubtestScoreDetail) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2E6D8)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detail.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            // Progress bar kecil atau Text Skor
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${detail.score}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (detail.score >= 700) Color(0xFF30D158) else Color(0xFFE61C5D)
                )
                Text("poin", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}