package com.example.tubespm.ui.screens.siswa.activity

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tubespm.data.model.LatihanSoal

@Composable
fun LatihanDalamProsesContent(
    latihanList: List<ActivityLatihanDetail>,
    onContinueClick: (ActivityLatihanDetail) -> Unit
){
    if (latihanList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada latihan yang sedang dikerjakan.")
        }
        return
    }

    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(latihanList) { activityDetail ->
            LatihanInProgressCard(
                activityDetail = activityDetail, // <-- Kirim data gabungan
                onContinueClick = { onContinueClick(activityDetail) }
            )
        }
    }
}

@Composable
fun LatihanInProgressCard(
    activityDetail: ActivityLatihanDetail, // <-- DIUBAH
    onContinueClick: () -> Unit
){
    // Ambil data dari model gabungan
    val latihan = activityDetail.latihanSoal
    val userActivity = activityDetail.userActivity

    // Hitung progress
    val progress = if (latihan.questionCount > 0) {
        userActivity.answeredQuestionCount.toFloat() / latihan.questionCount.toFloat()
    } else {
        0f // Hindari pembagian dengan nol
    }

    Card (
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column (
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                latihan.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            SubtestTag(text = latihan.subtest)
            Spacer(Modifier.height(12.dp))
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    "Progress",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    // --- Gunakan data live ---
                    "${userActivity.answeredQuestionCount} / ${latihan.questionCount} soal",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress }, // <-- Gunakan progress yang dihitung
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(12.dp))
            ActionButton(text = "Lanjutkan Latihan", onClick = onContinueClick)
        }
    }
}