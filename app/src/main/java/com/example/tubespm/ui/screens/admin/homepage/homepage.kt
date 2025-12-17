package com.example.tubespm.ui.screens.admin.homepage

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.max

@Composable
fun AdminHomeScreen(
    paddingValues: PaddingValues,
    viewModel: AdminHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF5F5F5))
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF6F61))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                // ===================== HEADER =====================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFF6F61), Color(0xFFE91E63))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Welcome,", color = Color.White, fontSize = 20.sp)
                            Text(
                                uiState.adminName,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        // --- BAGIAN INI YANG DIGANTI ---
                        // Memanggil fungsi helper di bawah untuk memunculkan gambar
                        HomeProfilePicture(base64String = uiState.adminPhotoBase64)
                    }
                }

                // ===================== CONTENT =====================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White,
                    tonalElevation = 1.dp
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AdminStatCard(
                                        title = "Paket Tryout\nAktif",
                                        bigText = uiState.paketTryoutAktif.toString(),
                                        subtitle = "Paket aktif",
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminStatCard(
                                        title = "Latihan Soal\nAktif",
                                        bigText = uiState.soalLatihan.toString(),
                                        subtitle = "Soal",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AdminStatCard(
                                        title = "Siswa Aktif",
                                        bigText = uiState.siswaAktif.toString(),
                                        subtitle = "Siswa",
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminStatCard(
                                        title = "Soal Dikerjakan",
                                        bigText = uiState.soalDikerjakan.toString(),
                                        subtitle = "Soal",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        item {
                            ActivityChartCard(
                                points = uiState.chartPoints,
                                selectedFilter = uiState.chartFilter,
                                isLoading = uiState.isChartLoading,
                                total = uiState.chartTotal,
                                onFilterChange = { viewModel.loadActivityChart(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// 1. HELPER BARU: FUNGSI UNTUK MUNCULIN GAMBAR
// ==========================================================
@Composable
fun HomeProfilePicture(base64String: String) {
    // Logika mengubah text aneh (base64) menjadi Gambar (Bitmap)
    val decodedBitmap = remember(base64String) {
        if (base64String.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        } else { null }
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0x33FFFFFF))
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (decodedBitmap != null) {
            // Kalau ada gambar, tampilkan gambar
            Image(
                bitmap = decodedBitmap,
                contentDescription = "Admin Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Kalau tidak ada, tampilkan Icon Orang
            Icon(
                Icons.Default.Person,
                tint = Color.White,
                contentDescription = "Default Profile",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ... Bagian bawah ini (Card & Chart) SAMA PERSIS, tidak ada yang diubah ...

@Composable
private fun AdminStatCard(
    title: String,
    bigText: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 140.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF29A3A),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontSize = 14.sp)
            Text(bigText, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFFFF3E0), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ActivityChartCard(
    points: List<ActivityPoint> = emptyList(),
    selectedFilter: ActivityRange = ActivityRange.WEEKLY,
    isLoading: Boolean = false,
    total: Int = 0,
    onFilterChange: (ActivityRange) -> Unit = {}
) {
    val filterOptions = listOf(
        ActivityRange.DAILY to "Daily",
        ActivityRange.WEEKLY to "Weekly",
        ActivityRange.MONTHLY to "Monthly"
    )
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text("Rata-rata Aktivitas", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    Text(
                        "Pengerjaan Soal",
                        fontSize = 18.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Bold
                    )
                }

                Box {
                    AssistChip(
                        onClick = { expanded = true },
                        label = {
                            Text(
                                filterOptions.first { it.first == selectedFilter }.second,
                                fontSize = 12.sp,
                                color = Color(0xFF333333)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Filter",
                                tint = Color(0xFF333333)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFF7F7F7),
                            labelColor = Color(0xFF333333)
                        )
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color.White
                    ) {
                        filterOptions.forEach { (range, label) ->
                            DropdownMenuItem(
                                text = { Text(text = label, color = Color.Black, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    expanded = false
                                    if (range != selectedFilter) onFilterChange(range)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF29A3A))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(total.toString(), color = Color.White, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF29A3A))
                    }
                } else {
                    val maxValue = max(points.maxOfOrNull { it.value } ?: 0, 1)
                    if (points.isEmpty()) {
                        Text("Belum ada aktivitas", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            points.forEach { point ->
                                val barHeight = (point.value.toFloat() / maxValue.toFloat()) * 100f
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(if (point.value == maxValue) 22.dp else 14.dp)
                                            .height(barHeight.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFF29A3A))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(point.label, fontSize = 10.sp, color = Color(0xFF616161))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}