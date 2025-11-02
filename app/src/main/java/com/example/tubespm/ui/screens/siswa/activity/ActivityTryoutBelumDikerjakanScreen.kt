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
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.TryoutSection

@Composable
fun TryoutBelumDikerjakanContent(tryouts: List<Tryout>, onCardClick: (Tryout) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(tryouts) { tryout ->
            TryoutActivityCard(
                tryout = tryout,
                onClick = { onCardClick(tryout) }
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
                    TryoutSectionRow(section = section)
                }
            }
        }
    }
}

@Composable
fun TryoutSectionRow(section: TryoutSection) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.9f)).padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(section.title, color = Color(0xFFE61C5D), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.weight(1f))
        InfoRow(icon = Icons.Filled.Description, text = "${section.totalQuestions} soal")
        Spacer(Modifier.width(16.dp))
        InfoRow(icon = Icons.Filled.Timer, text = "${section.totalDuration} menit")
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
    onCancel: () -> Unit // Parameter onCancel tetap ada
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
                // DIUBAH: Header sekarang dinamis
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

                // Detail konten tetap sama
                Text("Detail Tryout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("Kode Paket: ${tryout.code}", style = MaterialTheme.typography.bodyMedium)
                Text("Jumlah Soal: ${tryout.totalQuestions}", style = MaterialTheme.typography.bodyMedium)
                Text("Durasi: ${tryout.totalDuration} menit", style = MaterialTheme.typography.bodyMedium)
                Divider(Modifier.padding(vertical = 12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(tryout.sections) { section ->
                        Text(section.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            section.subSections.forEachIndexed { index, subSection ->
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(
                                        "${index + 1}. ${subSection.name} (${subSection.questionCount} soal, ${subSection.duration} menit)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (subSection.kisiKisi.isNotEmpty()) {
                                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                                            subSection.kisiKisi.forEach { kisi ->
                                                Text(
                                                    text = "- $kisi", // DIUBAH: Bullet point disesuaikan
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