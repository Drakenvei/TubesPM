package com.example.tubespm.ui.screens.siswa.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tubespm.data.model.Section
import com.example.tubespm.data.model.Tryout

@Composable
fun TryoutBelumDikerjakanContent(
    activities: List<ActivityTryoutDetail>,
    onCardClick: (ActivityTryoutDetail) -> Unit
) {
    if (activities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada tryout yang belum dikerjakan.")
        }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(activities) { activityDetail ->
            TryoutActivityCard(
                tryout = activityDetail.tryout, //data dari UserActivity
                onClick = { onCardClick(activityDetail) }
            )
        }
    }
}

// ... (Kode untuk TryoutActivityCard dan TryoutSectionRow tidak perlu diubah)
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
                    // Kita bisa panggil TryoutSectionRow karena kita punya data 'section'
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
        InfoRow(icon = Icons.Filled.Description, text = "${section.sectionQuestionCount} soal")
        Spacer(Modifier.width(16.dp))
        InfoRow(icon = Icons.Filled.Timer, text = "${section.sectionDuration} menit")
    }
}

// =======================================================================
// DIALOG DENGAN TAMPILAN MIRIP, TAPI FUNGSI TETAP SAMA (2 TOMBOL)
// =======================================================================
@Composable
fun TryoutDetailDialog(
    tryout: Tryout,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp).background(Color.LightGray.copy(alpha = 0.3f), CircleShape).padding(4.dp)
                        )
                    }
                    Text(
                        text = tryout.title, // Menggunakan judul tryout
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(Modifier.height(16.dp))

                Text("Detail Tryout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("Kode Paket: ${tryout.code}", style = MaterialTheme.typography.bodyMedium)
                Text("Jumlah Soal: ${tryout.totalQuestionCount}", style = MaterialTheme.typography.bodyMedium)
                Text("Durasi: ${tryout.totalDuration} menit", style = MaterialTheme.typography.bodyMedium)
                Divider(Modifier.padding(vertical = 12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(tryout.sections) { section ->
                        Text(section.sectionName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            section.subtests.forEachIndexed { index, subtest ->
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(
                                        "${index + 1}. ${subtest.subtestName} (${subtest.questionCount} soal, ${subtest.duration} menit)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (subtest.topics.isNotEmpty()) {
                                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                                            subtest.topics.forEach { topic ->
                                                Text(
                                                    text = "- ${topic.name}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // FUNGSI AWAL TETAP: Ada 2 tombol, tapi gaya tombol utama disesuaikan
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)), // DIUBAH: Gaya tombol utama
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Kerjakan Tryout", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE61C5D)), // TETAP: Tombol sekunder
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Batalkan Tryout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}