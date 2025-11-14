package com.example.tubespm.ui.screens.admin.profile // Pastikan package Anda benar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

// Dummy data untuk representasi gambar (gunakan Coil/Glide untuk gambar nyata)
val AdminProfileImage: @Composable () -> Unit = {
    // Placeholder untuk gambar profil
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Admin Profile Picture",
            tint = Color(0xFFFF6F61),
            modifier = Modifier.size(60.dp)
        )
    }
}

@Composable
fun AdminProfileScreen(
    // 1. TAMBAHKAN 'paddingValues' SEBAGAI PARAMETER
    paddingValues: PaddingValues,
    adminName: String = "Admin",
    adminEmail: String = "admin@gmail.com",
    onLogoutClick: () -> Unit = {}
) {
    // 2. HAPUS 'Scaffold' DARI SINI
    // 'Column' sekarang menjadi container utama
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            // 3. TERAPKAN 'paddingValues' YANG DITERIMA DARI FUNGSI
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ===================== HEADER MERAH & FOTO PROFIL =====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFF6F61), Color(0xFFE91E63))
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Background bubbles (untuk detail, bisa diabaikan atau diganti)
            //
            Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AdminProfileImage()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = adminName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = adminEmail,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )
            }
        }

        // ===================== KONTEN UTAMA (Kartu Putih) =====================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-60).dp) // Geser ke atas agar menutupi header
                .padding(horizontal = 24.dp)
        ) {

            // ---------- GRID STATISTIK ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatCard(title = "Jumlah User", value = "10", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(title = "Jumlah Tryout", value = "5", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(title = "Jumlah Latihan Soal", value = "5", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- FORM PROFIL ----------
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    ProfileInfoRow(label = "Nama:", value = adminName, isActionable = false)
                    CustomDivider() // Ganti nama agar tidak konflik
                    ProfileInfoRow(label = "Email:", value = adminEmail, isActionable = false)
                    CustomDivider()
                    ProfileInfoRow(label = "Password:", value = "Change", isActionable = true, onClick = { /* TODO: Navigate to Change Password */ })
                    CustomDivider()
                    ProfileInfoRow(label = "Profile Picture:", value = "Change", isActionable = true, onClick = { /* TODO: Handle Picture Change */ })
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ---------- TOMBOL LOGOUT ----------
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6F61) // Warna merah sesuai gambar
                )
            ) {
                Text("Log Out", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp)) // Padding di bawah
        }
    }
}

// ===================== KOMPONEN REUSABLE =====================

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    isActionable: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label (Nama, Email, dll)
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF616161),
            modifier = Modifier.width(130.dp) // Lebar tetap untuk label
        )

        Spacer(modifier = Modifier.weight(1f))

        // Nilai atau Tombol 'Change'
        if (isActionable) {
            TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = value,
                    color = Color(0xFFFF6F61), // Warna merah untuk 'Change'
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Saya ganti nama 'Divider' menjadi 'CustomDivider' untuk menghindari
// kemungkinan konflik nama jika Anda mengimpor 'Divider' dari M2 dan M3
@Composable
fun CustomDivider() {
    Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
}

// ===================== PREVIEW =====================

@Preview(showBackground = true)
@Composable
fun AdminProfileScreenPreview() {
    TubesPMTheme {
        // 4. PERBAIKI PREVIEW
        // Berikan 'paddingValues' kosong agar preview tidak error
        AdminProfileScreen(paddingValues = PaddingValues(0.dp))
    }
}