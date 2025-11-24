package com.example.tubespm.ui.screens.siswa.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TryoutDalamProsesContent(
    activities: List<ActivityTryoutDetail>,
    onContinueClick: (ActivityTryoutDetail) -> Unit
) {
    if (activities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada tryout yang sedang dikerjakan.")
        }
        return
    }

    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activities) { activityDetail ->
            TryoutInProgressCard(
                activityDetail = activityDetail,
                onContinueClick = { onContinueClick(activityDetail) }
            )
        }
    }
}

@Composable
fun TryoutInProgressCard(
    activityDetail: ActivityTryoutDetail, // <-- DIUBAH: Terima model gabungan
    onContinueClick: () -> Unit
) {
    val tryout = activityDetail.tryout
    val userActivity = activityDetail.userActivity

    // Hitung progres
    val progress = if (tryout.totalQuestionCount > 0) {
        // Ambil data baru dari userActivity
        userActivity.answeredQuestionCount.toFloat() / tryout.totalQuestionCount.toFloat()
    } else {
        0f // Hindari pembagian dengan nol
    }

    Card (
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column (
            Modifier.padding(16.dp)
        ) {
            // 1. Judul Tryout
            Text(
                tryout.title,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // --- BAGIAN PROGRESS BAR (BARU) ---
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Progress:",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    // Tampilkan "15 / 150 soal"
                    "${userActivity.answeredQuestionCount} / ${tryout.totalQuestionCount} soal",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress }, // <-- Set nilai progress
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp) // Buat sedikit lebih tebal
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White, // Warna progress
                trackColor = Color.White.copy(alpha = 0.3f) // Warna background
            )
            // --- AKHIR BAGIAN PROGRESS BAR ---

            Divider(
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 2. Daftar Section (re-use Composable yang ada)
            Column (
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tryout.sections.forEach { section ->
                    TryoutSectionRow(section = section)
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Tombol Lanjutkan
            Button(
                onClick = onContinueClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    "Lanjutkan Tryout",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}