package com.example.tubespm.ui.screens.admin.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val paketTryoutAktif = 25
    val soalLatihan = 2456
    val siswaAktif = 2456
    val soalDikerjakan = 2456

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF5F5F5))
    ) {
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
                            adminName,
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
                            Icons.Default.Person,
                            tint = Color.White,
                            contentDescription = "Admin Profile"
                        )
                    }
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
                    }
                    item { ActivityChartCard() }
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
private fun ActivityChartCard() {
    val filterOptions = listOf("Daily", "Weekly", "Monthly")
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Weekly") }

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

                // ===== CHIP DROPDOWN =====
                Box {
                    AssistChip(
                        onClick = { expanded = true },
                        label = {
                            Text(
                                selectedFilter,
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

                    // ===== DROPDOWN MENU =====
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color(0xFF1A1A1A) // dark dropdown
                    ) {
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        color = Color.White,   // WHITE TEXT
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    selectedFilter = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== CHART VALUE =====
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF29A3A))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("2,313", color = Color.White, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ===== BAR CHART =====
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    val heights = listOf(50, 35, 65, 55, 110, 40, 45)

                    days.forEachIndexed { index, day ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(if (day == "FRI") 22.dp else 14.dp)
                                    .height(heights[index].dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF29A3A))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(day, fontSize = 10.sp, color = Color(0xFF616161))
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
        AdminHomeScreen(PaddingValues(0.dp))
    }
}
