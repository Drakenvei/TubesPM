package com.example.tubespm.ui.screens.siswa.activity.tryout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TryoutSelesaiContent(
    activities: List<ActivityTryoutDetail>,
    onResultClick: (ActivityTryoutDetail) -> Unit
) {
    if (activities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada tryout yang telah selesai.")
        }
        return
    }

    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activities) { activityDetail ->
            TryoutCompletedCard(
                activityDetail = activityDetail,
                onResultClick = { onResultClick(activityDetail) }
            )
        }
    }
}

/**
 * Card baru untuk item yang sudah selesai.
 * Menampilkan Skor Total + detail section + tombol Pembahasan.
 */
@Composable
fun TryoutCompletedCard(
    activityDetail: ActivityTryoutDetail,
    onResultClick: () -> Unit
) {
    val tryout = activityDetail.tryout
    val userActivity = activityDetail.userActivity

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column(
            Modifier.padding(16.dp)
        ) {
            // 1. Judul Tryout
            Text(
                tryout.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // 2. Info Skor Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Skor Akhir:",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = userActivity.score.toString(), // <-- Ambil skor
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                // <-- Ambil info jawaban benar
                "Jawaban Benar: ${userActivity.correctCount} / ${tryout.totalQuestionCount} soal",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )

            Divider(
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 3. Daftar Section (re-use Composable)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tryout.sections.forEach { section ->
                    // Kita gunakan TryoutSectionRow yang sama, karena ini hanya info metadata
                    TryoutSectionRow(section = section)
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. Tombol Lihat Pembahasan
            Button(
                onClick = onResultClick,
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
}