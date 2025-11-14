package com.example.tubespm.ui.screens.siswa.activity.latihan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tubespm.data.model.LatihanCompleted

@Composable
fun LatihanSelesaiContent(
    latihanList: List<LatihanCompleted>,
    navController: NavController  // Tambahkan parameter
){
    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(latihanList) { latihan ->
            LatihanCompletedCard(
                latihan = latihan,
                navController = navController  // Pass ke card
            )
        }
    }
}

@Composable
fun LatihanCompletedCard(latihan: LatihanCompleted, navController: NavController) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = latihan.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            SubtestTag(text = latihan.subtest)
            Spacer(Modifier.height(12.dp))

            // Baris ini juga akan rata kiri
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Jawaban Benar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Jawaban benar: ${latihan.correctAnswers}/${latihan.totalQuestions} soal",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(12.dp))
            ActionButton(
                text = "Lihat Pembahasan",
                onClick = {
                    // Navigasi ke screen pembahasan
                    navController.navigate("pembahasan_latihan")
                }
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tanggal Dikerjakan: ${latihan.completionDate}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}