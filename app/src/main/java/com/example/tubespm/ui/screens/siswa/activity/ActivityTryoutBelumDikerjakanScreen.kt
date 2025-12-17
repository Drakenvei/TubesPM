package com.example.tubespm.ui.screens.siswa.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.data.model.Section
import com.example.tubespm.data.model.Tryout

// Catatan: InfoRow didefinisikan di sini agar TryoutSectionRow dapat mengkompilasi.
// Di proyek Anda yang sebenarnya, pastikan InfoRow hanya didefinisikan sekali di file utility.
@Composable
fun InfoRow(icon: ImageVector, text: String, tint: Color = Color.White) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = tint, style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
fun TryoutBelumDikerjakanContent(
    activities: List<ActivityTryoutDetail>,
    onCardClick: (ActivityTryoutDetail) -> Unit,
    onTryoutCanceled: (ActivityTryoutDetail) -> Unit
) {
    if (activities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada tryout yang belum dikerjakan.")
        }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(activities) { activityDetail ->
            TryoutActivityCard(
                tryout = activityDetail.tryout,
                onClick = { onCardClick(activityDetail) }
            )
        }
    }
    // Semua logika dialog sudah di ActivityTryoutScreen.kt
}


@Composable
fun TryoutActivityCard(tryout: Tryout, onClick: () -> Unit){
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(tryout.title, style = MaterialTheme.typography.titleMedium.copy(color = Color.White), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tryout.sections.forEach { section ->
                    TryoutSectionRow(section = section)
                }
            }
        }
    }
}

@Composable
fun TryoutSectionRow(section: Section) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.9f)).padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(section.sectionId.uppercase(), color = Color(0xFFE61C5D), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.weight(1f))

        // Panggilan InfoRow dengan parameter tint untuk warna putih di atas kartu merah
        InfoRow(icon = Icons.Filled.Description, text = "${section.sectionQuestionCount} soal", tint = Color.White)
        Spacer(Modifier.width(16.dp))
        InfoRow(icon = Icons.Filled.Timer, text = "${section.sectionDuration} menit", tint = Color.White)
    }
}

// Catatan: TryoutDetailDialog sudah dihapus dari sini.