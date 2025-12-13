package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets

// ======================================================
// SCREEN UTAMA — TAB "Latihan Soal"
// ======================================================
@Composable
fun LatihanSoalTabContent(
    contentPadding: PaddingValues,
    onAddClick: () -> Unit,
    onEditClick: (String, String, String, String, Int) -> Unit, // (type, latihanId, questionId, paketName, questionNumber)
    onGoToListSoal: (String, String, String) -> Unit = { _, _, _ -> }, // (type, parentId, paketName)
    viewModel: ManajemenLatihanSoalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.offset(x = 0.dp, y = (-80).dp),
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(innerPadding)
                .padding(contentPadding)
        ) {

            // ===============================
            // Search Bar (Style diperbarui sedikit agar rounded)
            // ===============================
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Cari Latihan Soal...", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray)
                },
                shape = RoundedCornerShape(12.dp), // Lebih rounded seperti siswa
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            // ===============================
            // LIST PAKET SOAL
            // ===============================
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE61C5D))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.paketSoalList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Data tidak ditemukan", color = Color.Gray)
                            }
                        }
                    } else {
                        items(uiState.paketSoalList) { paket ->
                            PaketSoalAdminCard(
                                paket = paket,
                                onEditClick = {
                                    // Navigate to edit latihan soal (nama dulu)
                                    onEditClick(
                                        "latihan_soal",  // Type: latihan_soal
                                        paket.id,
                                        "edit_nama", // Special ID untuk edit nama
                                        paket.nama,
                                        0
                                    )
                                },
                                onGoToListSoal = {
                                    // Navigate directly to list soal
                                    onGoToListSoal("latihan_soal", paket.id, paket.nama)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================================================
// CARD ITEM BARU (Style User + Tombol Edit Admin)
// ======================================================
@Composable
fun PaketSoalAdminCard(
    paket: PaketSoal,
    onEditClick: () -> Unit = {},
    onGoToListSoal: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        // Warna Pink persis seperti tampilan Siswa (0xFFE61C5D)
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // --- Row Atas: Judul & Tombol Edit ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top, // Align top jika teks panjang
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = paket.nama,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontSize = 18.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f) // Text ambil sisa ruang
                )

                // Row untuk tombol-tombol
                Row {
                    // Tombol Lihat Soal (Baru)
                    TextButton(
                        onClick = onGoToListSoal,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Lihat Soal", fontSize = 12.sp)
                    }

                    // Tombol Edit (Khas Admin)
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(

                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Subtest Tag (Khas Tampilan Siswa) ---
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = paket.subtest, // Menggunakan data subtest dari VM
                    color = Color(0xFFE61C5D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Footer: Jumlah Soal dan Status ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = "Jumlah Soal",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${paket.totalSoal} soal",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Status Badge
                Surface(
                    color = if (paket.isActive) Color.White else Color(0xFFB0BEC5), // Putih untuk active, abu untuk inactive
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (paket.isActive) "active" else "inactive", // Ganti dari Aktif/Nonaktif
                        color = if (paket.isActive) Color(0xFFE61C5D) else Color.White, // Pink untuk active, putih untuk inactive
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}