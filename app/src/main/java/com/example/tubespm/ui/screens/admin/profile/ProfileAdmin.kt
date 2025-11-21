package com.example.tubespm.ui.screens.admin.profile

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Helper Composable untuk menampilkan Base64 atau Placeholder
// Helper Composable untuk menampilkan Base64 atau Placeholder
@Composable
fun AdminProfileImage(base64String: String, onClick: () -> Unit) {
    // 1. Lakukan decoding di sini (LOGIKA SAJA), gunakan remember agar efisien
    val decodedBitmap = remember(base64String) {
        if (base64String.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onClick() }, // Klik untuk ganti foto
        contentAlignment = Alignment.Center
    ) {
        // 2. Tampilkan UI berdasarkan hasil decoding (TANPA TRY-CATCH DI SINI)
        if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap.asImageBitmap(),
                contentDescription = "Admin Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            // Fallback jika decode gagal atau string kosong
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile",
                tint = Color(0xFFFF6F61),
                modifier = Modifier.size(60.dp)
            )
        }
    }
}
@Composable
fun AdminProfileScreen(
    paddingValues: PaddingValues,
    onLogoutClick: () -> Unit,
    viewModel: AdminProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher untuk memilih foto dari galeri
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateProfilePicture(context, uri)
            }
        }
    )

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF6F61))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                Column(
                    modifier = Modifier.padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Panggil Composable Gambar dengan fungsi klik
                    AdminProfileImage(
                        base64String = uiState.profilePictureBase64,
                        onClick = {
                            // Buka Galeri
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = uiState.name,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.email,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp
                    )
                }
            }

            // ===================== KONTEN UTAMA (Kartu Putih) =====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-60).dp)
                    .padding(horizontal = 24.dp)
            ) {

                // ---------- GRID STATISTIK ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatCard("Jumlah User", uiState.userCount, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(10.dp))
                    StatCard("Jumlah Tryout", uiState.tryoutCount, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(10.dp))
                    StatCard("Jumlah Latihan", uiState.exerciseCount, Modifier.weight(1f))
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
                        ProfileInfoRow(label = "Nama:", value = uiState.name, isActionable = false)
                        CustomDivider()
                        ProfileInfoRow(label = "Email:", value = uiState.email, isActionable = false)
                        CustomDivider()
                        // Tombol Edit Password (bisa ditambahkan fungsi nanti)
                        ProfileInfoRow(
                            label = "Password:",
                            value = "Change",
                            isActionable = true,
                            onClick = { /* TODO: Navigasi ke ganti password */ }
                        )
                        CustomDivider()
                        // Tombol Edit Foto (alternatif selain klik gambar)
                        ProfileInfoRow(
                            label = "Profile Picture:",
                            value = "Change",
                            isActionable = true,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61))
                ) {
                    Text("Log Out", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ... (StatCard, ProfileInfoRow, CustomDivider biarkan sama seperti sebelumnya) ...
@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 90.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF616161),
            modifier = Modifier.width(130.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isActionable) {
            TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                Text(text = value, color = Color(0xFFFF6F61), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        } else {
            Text(text = value, fontSize = 16.sp, color = Color(0xFF333333), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CustomDivider() {
    Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
}