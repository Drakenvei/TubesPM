package com.example.tubespm.ui.screens.siswa.profile

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast // Import Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Import Context
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
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current // Butuh context untuk Toast

    // --- LOGIKA NAVIGASI & ERROR HANDLING ---
    LaunchedEffect(uiState.isSaveSuccess, uiState.error) {
        if (uiState.isSaveSuccess) {
            Toast.makeText(context, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
            onBackClick() // Keluar layar
            viewModel.resetState()
        }

        if (uiState.error != null) {
            Toast.makeText(context, "Error: ${uiState.error}", Toast.LENGTH_LONG).show()
            viewModel.resetState() // Reset error agar tidak muncul terus
        }
    }
    // ----------------------------------------

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
        TopAppBar(
            title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE61C5D))
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... (Bagian Kode Gambar Profile Sama Seperti Sebelumnya) ...
                Box(modifier = Modifier.size(100.dp)) {
                    if (uiState.newSelectedImageUri != null) {
                        AsyncImage(
                            model = uiState.newSelectedImageUri,
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color(0xFFE61C5D), CircleShape)
                        )
                    } else {
                        val decodedBitmap = remember(uiState.currentProfileImageUrl) {
                            if (uiState.currentProfileImageUrl.isNotEmpty()) {
                                try {
                                    val bytes = Base64.decode(uiState.currentProfileImageUrl, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (e: Exception) { null }
                            } else null
                        }
                        if (decodedBitmap != null) {
                            Image(
                                bitmap = decodedBitmap.asImageBitmap(),
                                contentDescription = "Current Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color(0xFFE61C5D), CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.user_default_profile),
                                contentDescription = "Default",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color(0xFFE61C5D), CircleShape)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800))
                            .border(2.dp, Color(0xFFE61C5D), CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                // ... (End Bagian Gambar) ...

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

                Button(
                    // PANGGIL saveProfile TANPA parameter onSuccess (sudah dihandle LaunchedEffect)
                    onClick = { viewModel.saveProfile() },
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE61C5D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Simpan Perubahan")
                    }
                }
            }
        }
    }
}