package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

// ======================================================
// SCREEN UTAMA — TAB "Latihan Soal"
// ======================================================
@Composable
fun LatihanSoalTabContent(
    contentPadding: PaddingValues,
    viewModel: ManajemenLatihanSoalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9)) // Background sedikit lebih terang/bersih
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
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFE0E0E0),
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                        PaketSoalAdminCard(paket)
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
fun PaketSoalAdminCard(paket: PaketSoal) {
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

                // Tombol Edit (Khas Admin)
                // Dibuat semi-transparan putih agar menyatu
                IconButton(
                    onClick = { /* TODO: Navigasi ke Edit */ },
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = 8.dp, y = (-8).dp) // Sedikit geser ke pojok
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = Color.White
                    )
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

            // --- Footer: Jumlah Soal ---
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
        }
    }
}