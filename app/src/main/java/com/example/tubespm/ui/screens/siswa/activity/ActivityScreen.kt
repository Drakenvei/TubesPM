package com.example.tubespm.ui.screens.siswa.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(navController: NavController){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Daftar Tryout & Latihan Soal",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE61C5D) // Mengatur warna background
            )
        )

        Spacer(Modifier.height(8.dp))

        ActivityNavigationCard(
            title = "Daftar Tryout",
            count = 3,
            onClick = {
                // Aksi navigasi ke ActivityTryoutScreen()
                 navController.navigate("activity_tryout_list")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActivityNavigationCard(
            title = "Daftar Latihan Soal",
            count = 6,
            onClick = {
                navController.navigate("activity_latihan_list")
            }
        )
    }
}

@Composable
fun ActivityNavigationCard(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Row (
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Jumlah: $count",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Lanjutkan",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
fun ActivityScreenPreview() {
    ActivityScreen(navController = rememberNavController())
}