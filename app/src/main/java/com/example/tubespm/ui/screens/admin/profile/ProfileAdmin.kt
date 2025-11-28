package com.example.tubespm.ui.screens.admin.profile

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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

// Helper untuk menampilkan gambar Base64
@Composable
fun AdminProfileImage(
    base64String: String,
    isLoading: Boolean, // Tambah parameter loading
    onClick: () -> Unit
) {
    val decodedBitmap = remember(base64String) {
        if (base64String.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) { null }
        } else { null }
    }

    Box(
        modifier = Modifier
            .size(110.dp) // Sedikit diperbesar
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, Color.White, CircleShape)
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Tampilkan loading kecil di dalam lingkaran foto saat upload
            CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color(0xFFFF6F61))
        } else if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap.asImageBitmap(),
                contentDescription = "Admin Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default",
                tint = Color.Gray,
                modifier = Modifier.size(60.dp)
            )
        }

        // Ikon edit kecil (opsional)
        if(!isLoading){
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(12.dp).background(Color.Green, CircleShape))
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateProfilePicture(context, uri)
            }
        }
    )

    // Jangan block seluruh layar dengan loading, cukup disable interaksi
    // Loading hanya ditampilkan di dalam lingkaran foto (handled by AdminProfileImage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // HEADER GRADIENT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFF6F61), Color(0xFFD84315))
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AdminProfileImage(
                    base64String = uiState.profilePictureBase64,
                    isLoading = uiState.isLoading,
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.name.ifBlank { "Admin" },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.email,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
        }

        // CONTENT AREA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-50).dp) // Efek overlap ke atas
                .padding(horizontal = 20.dp)
        ) {
            // Grid Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("User", uiState.userCount, Modifier.weight(1f))
                StatCard("Tryout", uiState.tryoutCount, Modifier.weight(1f))
                StatCard("Latihan", uiState.exerciseCount, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Options
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ProfileInfoRow(label = "Nama Lengkap", value = uiState.name, isActionable = false)
                    CustomDivider()
                    ProfileInfoRow(label = "Email Admin", value = uiState.email, isActionable = false)
                    CustomDivider()
                    ProfileInfoRow(
                        label = "Ganti Foto",
                        value = "Upload",
                        isActionable = true,
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Keluar / Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Component Kecil Tetap Sama
@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF333333))
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, isActionable: Boolean, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().height(50.dp).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        if (isActionable) {
            Text(
                text = value,
                color = Color(0xFFFF6F61),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onClick() }
            )
        } else {
            Text(text = value, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun CustomDivider() {
    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
}