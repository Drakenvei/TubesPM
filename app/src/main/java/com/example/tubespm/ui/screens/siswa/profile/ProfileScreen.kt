package com.example.tubespm.ui.screens.siswa.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.tubespm.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel() // Inject ViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // 🔴 Header section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE91E63))
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable{ onEditClick()}
                )

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable{ onSettingsClick()}
                )
            }

            // Profile image
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp) // Tambah padding bottom
            ) {

                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    // Gunakan AsyncImage untuk memuat URL dari ViewModel
                    AsyncImage(
                        model = uiState.profileImageUrl, // Ambil dari state dan Menggunakan URL gambar
                        contentDescription = "Profile Image",
                        placeholder = painterResource(id = R.drawable.user_default_profile),
                        error = painterResource(id = R.drawable.user_default_profile),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp,Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 Dynamic user data
                Text(
                    text = uiState.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = uiState.email,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = uiState.school,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // --- Wrapper untuk Loading/Error ---
        when {
            uiState.isLoading -> {
                // Loading
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                // Error
                Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red,
                    modifier = Modifier.padding(24.dp)
                )
            }
            else -> {
                // Konten jika data berhasil dimuat
                // 🟠 Stats Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    StatCard(
                        uiState.tryoutCount.toString(),
                        "Paket tryout dikerjakan",
                        modifier = Modifier.fillMaxWidth()
                    )

                    StatCard(
                        uiState.latihanCount.toString(),
                        "Soal latihan dikerjakan",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 📦 Placeholder Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(horizontal = 24.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Dorong ke bawah
                }
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp), // Sesuaikan padding
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
