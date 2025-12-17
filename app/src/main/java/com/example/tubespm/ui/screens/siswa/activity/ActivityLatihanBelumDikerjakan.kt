package com.example.tubespm.ui.screens.siswa.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tubespm.data.model.LatihanSoal
// Asumsi model lain (ActivityLatihanDetail) di-import atau dideklarasikan di file lain.

@Composable
fun LatihanBelumDikerjakanContent(
    latihanList: List<ActivityLatihanDetail>,
    onCardClick: (ActivityLatihanDetail) -> Unit
){
    if (latihanList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada latihan yang belum dikerjakan.")
        }
        return
    }

    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(latihanList) { activityDetail->
            LatihanCard(
                latihan = activityDetail.latihanSoal,
                onClick = {onCardClick(activityDetail)}
            )
        }
    }
}

@Composable
fun LatihanCard(latihan: LatihanSoal, onClick: () -> Unit) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column (
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = latihan.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            // SubtestTag didefinisikan di ActivityLatihanScreen.kt
            // Jika Anda ingin menggunakannya di sini, Anda harus mendeklarasikannya di sini
            // atau memindahkannya ke file utility.
            // ASUMSI: SubtestTag adalah komponen yang ada.
            SubtestTag(text = latihan.subtest)
            Spacer(Modifier.height(12.dp))
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Description,
                    "Jumlah Soal",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "${latihan.questionCount} soal",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }
    }
}

// Dialog Detail Latihan harus ada di sini jika Anda ingin ActivityLatihanScreen.kt
// memanggilnya. Jika tidak, maka harus didefinisikan di ActivityLatihanScreen.kt
// Kita asumsikan tetap di sini (seperti kode awal Anda) agar ActivityLatihanScreen.kt
// tidak menjadi terlalu besar (walaupun penanganan state ada di parent).

@Composable
fun LatihanDetailDialog(
    latihan: LatihanSoal,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit, // onCancel ini sekarang hanya memicu state di ActivityLatihanScreen
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                                .padding(4.dp)
                        )
                    }
                    Text(
                        latihan.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Detail Latihan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Subtest: ${latihan.subtest}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Kode Paket: ${latihan.code}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Jumlah Soal: ${latihan.questionCount} soal",
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider(Modifier.padding(vertical = 12.dp))

                Text(
                    "Kisi-Kisi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                Column (
                    modifier = Modifier
                        .padding(start = 8.dp)
                ) {
                    latihan.topics.forEach { topic ->
                        Text(
                            "- ${topic.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                ActionButton(text = "Kerjakan Latihan", onClick = onStart)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onCancel, // onCancel ini memicu state di ActivityLatihanScreen
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE61C5D)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        "Batalkan Latihan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}