package com.example.tubespm.ui.screens.siswa.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.tubespm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel() // Inject ViewModel
) {
    // Ambil state dari viewModel
    val uiState by viewModel.uiState.collectAsState()

    // Buat Image Picker Launcher
    val  imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onImageSelected(it) // Kirim URI ke ViewModel
        }
    }

    // Fungsi untuk memicu image picker
    val onPhotoClick = {
        imagePickerLauncher.launch("image/*")
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            navigationIcon = {
                // Tombol kembali ditambahkan di sini
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE61C5D) // Mengatur warna background
            )
        )

        // Wrapper untuk loading
        if(uiState.isLoading){
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Konten Utama
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    // Gunakan Coil (AsyncImage)
                    // Ini akan menampilkan gambar baru (newSelectedImageUri) jika ada,
                    // atau gambar lama (currentProfileImageUrl) jika tidak.
                    AsyncImage(
                        model = uiState.newSelectedImageUri ?: uiState.currentProfileImageUrl,
                        contentDescription = "Profile Image",
                        placeholder = painterResource(id = R.drawable.user_default_profile),
                        error = painterResource(id = R.drawable.user_default_profile),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize() // Penuhi Box 100.dp
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFE61C5D), CircleShape)
                    )

                    // Tombol ganti foto
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800))
                            .border(2.dp, Color(0xFFE61C5D), CircleShape)
                            .clickable{ onPhotoClick() }, // Panggil launcher
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.school,
                    onValueChange = viewModel::onSchoolChanged,
                    label = { Text("Asal Sekolah") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hubungkan Tombol Simpan ke ViewModel
                Button(
                    onClick = {
                        viewModel.saveProfile(onSuccess = onBackClick)
                    },
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE61C5D)),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Simpan Perubahan")
                    }
                }

                // Tampilkan pesan error jika ada
                if(uiState.error != null){
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}