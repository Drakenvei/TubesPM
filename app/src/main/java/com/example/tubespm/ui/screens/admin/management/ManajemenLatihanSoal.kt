package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            // Search Bar
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
                shape = RoundedCornerShape(12.dp),
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
                                        "latihan_soal",
                                        paket.id,
                                        "edit_nama",
                                        paket.nama,
                                        0
                                    )
                                },
                                onGoToListSoal = {
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
// CARD ITEM BARU (Dengan Statistik)
// ======================================================
@Composable
fun PaketSoalAdminCard(
    paket: PaketSoal,
    onEditClick: () -> Unit = {},
    onGoToListSoal: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Background Putih agar bersih
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // --- Row Atas: Header Info & Tombol ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Subtest Tag
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = paket.subtest,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Judul
                    Text(
                        text = paket.nama,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF212121),
                            fontSize = 18.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tanggal Pembuatan
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dibuat: ${formatDate(paket.createdAt)}",
                            fontSize = 11.sp, color = Color.Gray
                        )
                    }
                }

                // Tombol Aksi (Lihat & Edit)
                Row {
                    IconButton(
                        onClick = onGoToListSoal,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = "Lihat Soal",
                            tint = Color(0xFF2196F3)
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF757575)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

            // --- Footer: Statistik & Status ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Center vertikal untuk row statistik
            ) {
                // Kolom Kiri: Statistik User & Soal
                Column {
                    StatItem(Icons.Filled.AssignmentTurnedIn, "${paket.attemptCount} Selesai")
                    Spacer(modifier = Modifier.height(4.dp))
                    StatItem(Icons.Filled.Description, "${paket.totalSoal} Soal")
                }

                // Kolom Kanan: Rata-rata Skor & Status Badge
                Column(horizontalAlignment = Alignment.End) {
                    StatItem(
                        icon = Icons.Filled.Analytics,
                        value = "Avg: ${"%.1f".format(paket.averageScore)}",
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status Badge
                    Surface(
                        color = if (paket.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (paket.isActive) "Active" else "Inactive",
                            color = if (paket.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}