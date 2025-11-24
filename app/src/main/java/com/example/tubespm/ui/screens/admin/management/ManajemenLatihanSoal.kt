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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Pastikan import ini ada

// ======================================================
// DATA MODEL (UI Helper)
// ======================================================
data class PaketSoal(
    val id: String,
    val nama: String,
    val tpsCount: Int,
    val literasiCount: Int,
    val tpsMenit: Int = 0,
    val literasiMenit: Int = 0
)

// ======================================================
// SCREEN UTAMA — DIGUNAKAN DI TAB "Latihan Soal"
// ======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatihanSoalTabContent(
    contentPadding: PaddingValues,
    // Inject ViewModel
    viewModel: ManajemenLatihanSoalViewModel = viewModel()
) {
    // Observasi State dari ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(contentPadding)
    ) {

        // ===============================
        // Search Bar
        // ===============================
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) }, // Kirim event ke VM
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Search Latihan Soal", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            shape = RoundedCornerShape(8.dp),
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
                CircularProgressIndicator(color = Color(0xFFE91E63))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.paketSoalList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Data tidak ditemukan", color = Color.Gray)
                        }
                    }
                } else {
                    items(uiState.paketSoalList) { paket ->
                        PaketSoalCard(paket)
                    }
                }
            }
        }
    }
}

// ======================================================
// CARD ITEM (Tidak berubah, tetap UI murni)
// ======================================================
@Composable
fun PaketSoalCard(paket: PaketSoal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE91E63)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // ===============================
            // Header: Nama + Edit Icon
            // ===============================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = paket.nama,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { /* TODO: nanti edit paket soal */ }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        tint = Color.White,
                        contentDescription = "Edit Paket"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===============================
            // TPS SECTION
            // ===============================
            PaketSoalSection(title = "TPS", soalCount = paket.tpsCount)

            Spacer(modifier = Modifier.height(12.dp))

            // ===============================
            // LITERASI SECTION
            // ===============================
            PaketSoalSection(title = "Literasi", soalCount = paket.literasiCount)
        }
    }
}

// ======================================================
// SECTION REUSABLE (Tidak berubah)
// ======================================================
@Composable
fun PaketSoalSection(
    title: String,
    soalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label TPS / Literasi
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFFE91E63),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Ikon jumlah soal
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                tint = Color.White,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (soalCount > 0) "$soalCount soal" else "- soal",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}