package com.example.tubespm.ui.screens.admin.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.ui.theme.TubesPMTheme

@Composable
fun AdminHomeScreen(
    paddingValues: PaddingValues,
    adminName: String = "Admin"
) {
    // dummy data
    val paketTryoutAktif = 25
    val soalLatihan = 2456
    val siswaAktif = 2456
    val soalDikerjakan = 2456

    val scrollState = rememberScrollState()

    // Column terluar (container utama)
    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(paddingValues) // Terapkan padding dari Scaffold
            .background(Color(0xFFF5F5F5))
        // ⬅️ 1. HAPUS .verticalScroll(scrollState) DARI SINI
    ) {

        // ===================== HEADER =====================
        // Header ini sekarang TIDAK akan ikut scroll
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
                    Text(
                        text = "Welcome,",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        text = adminName,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Admin Profile",
                        tint = Color.White
                    )
                }
            }
        }

        // ===================== KONTEN PUTIH =====================
        Surface(
            // ⬅️ 2. UBAH .fillMaxSize() MENJADI .weight(1f) & .fillMaxWidth()
            // Ini membuat Surface mengisi sisa ruang yang tersedia di Column
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            tonalElevation = 1.dp
        ) {
            // Column ini berisi konten yang BISA di-scroll
            Column(
                modifier = Modifier
                    // ⬅️ 3. TAMBAHKAN .verticalScroll(scrollState) & .fillMaxSize() DI SINI
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 20.dp) // Hapus padding bottom di sini
            ) {

                // ---------- GRID CARD 2x2 ----------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminStatCard(
                            title = "Paket Tryout\nAktif",
                            bigText = paketTryoutAktif.toString(),
                            subtitle = "Paket aktif",
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            title = "Soal Latihan",
                            bigText = soalLatihan.toString(),
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
                            bigText = siswaAktif.toString(),
                            subtitle = "Siswa",
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            title = "Soal Dikerjakan",
                            bigText = soalDikerjakan.toString(),
                            subtitle = "Soal",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---------- KARTU GRAFIK ----------
                ActivityChartCard()

                // ⬅️ 4. (Opsional tapi disarankan) Tambah Spacer di akhir
                // Agar kartu grafik tidak menempel di bagian bawah saat di-scroll
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
private fun AdminStatCard(
    // ... (Kode tidak berubah)
    title: String,
    bigText: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 140.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF29A3A),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = bigText,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFFFFF3E0),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ActivityChartCard() {
    // ... (Kode tidak berubah)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Rata-rata Aktivitas",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = "Pengerjaan Soal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }
                AssistChip(
                    onClick = { /* TODO: ubah filter */ },
                    label = {
                        Text(
                            text = "Weekly",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Filter"
                        )
                    },
                    shape = RoundedCornerShape(50)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF29A3A))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "2,313",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    val heights = listOf(50, 35, 65, 55, 110, 40, 45)
                    days.forEachIndexed { index, day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(if (day == "FRI") 22.dp else 14.dp)
                                    .height(heights[index].dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF29A3A))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day,
                                fontSize = 10.sp,
                                color = Color(0xFF616161)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomeScreenPreview() {
    TubesPMTheme {
        AdminHomeScreen(paddingValues = PaddingValues(0.dp))
    }
}