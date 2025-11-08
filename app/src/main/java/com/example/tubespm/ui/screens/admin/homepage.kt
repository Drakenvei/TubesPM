package com.example.tubespm.ui.screens.admin

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.ui.theme.TubesPMTheme

@Composable
fun AdminHomeScreen(
    adminName: String = "Admin"
) {
    // dummy data – nanti bisa dihubungkan ke Firestore / API
    val paketTryoutAktif = 25
    val soalLatihan = 2456
    val siswaAktif = 2456
    val soalDikerjakan = 2456

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        bottomBar = { AdminBottomNavigation() }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(scrollState)
        ) {

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
                        Text(
                            text = "Welcome,",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = adminName,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // lingkaran avatar
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
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 16.dp)
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
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    bigText: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 140.dp),      // lebih tinggi supaya teks tidak kepotong
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
                fontSize = 28.sp,        // sedikit lebih kecil biar lega
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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

            // --------- Judul + chip Weekly ---------
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
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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

            // --------- Grafik bar ---------
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // label angka bar tertinggi
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
                        .height(160.dp),   // sedikit lebih tinggi supaya label hari tidak kepotong
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

@Composable
private fun AdminBottomNavigation() {
    var selectedIndex by remember { mutableStateOf(0) }
    val items = listOf("Homepage", "Search", "Report", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Search,
        Icons.Default.ListAlt,
        Icons.Default.Person
    )

    NavigationBar {
        items.forEachIndexed { index, label ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = {
                    selectedIndex = index
                    // TODO: hubungkan dengan NavController kalau nanti mau pindah halaman beneran
                },
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = label
                    )
                },
                label = {
                    Text(text = label, fontSize = 11.sp)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomeScreenPreview() {
    TubesPMTheme {
        AdminHomeScreen()
    }
}
