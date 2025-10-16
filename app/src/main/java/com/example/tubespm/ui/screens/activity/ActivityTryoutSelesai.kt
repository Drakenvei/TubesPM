package com.example.tubespm.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.data.model.CompletedSectionState
import com.example.tubespm.data.model.TryoutCompleted

@Composable
fun TryoutSelesaiContent(tryouts: List<TryoutCompleted>) {
    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tryouts) { tryout ->
            TryoutCompletedCard( tryout = tryout)
        }
    }
}

@Composable
fun TryoutCompletedCard(tryout: TryoutCompleted) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column(
            Modifier.padding(16.dp)
        ) {
            Text(
                tryout.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tanggal Dikerjakan: ${tryout.completionDate}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tryout.sections.forEach { section ->
                    CompletedSection(section = section)
                    if (tryout.sections.last() != section) {
                        Divider(
                            color = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedSection(section: CompletedSectionState) {
    Column {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                section.title,
                color = Color(0xFFE61C5D),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        InfoRow(
            icon = Icons.Default.CheckCircle,
            text = "Jawaban benar: ${section.correctAnswers}/${section.totalQuestions} soal"
        )
        Spacer(Modifier.height(8.dp))
        InfoRow(
            icon = Icons.Default.HourglassBottom,
            text = "Waktu selesai: ${section.completionTime}"
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { /* TODO: Lihat Pembahasan */ },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                "Lihat Pembahasan",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}