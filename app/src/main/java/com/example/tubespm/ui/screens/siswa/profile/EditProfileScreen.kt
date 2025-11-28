package com.example.tubespm.ui.screens.siswa.profile

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast // Import Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Import Context
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tubespm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // ... (Bagian Kode Gambar Profile Sama Seperti Sebelumnya) ...
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.newSelectedImageUri != null) {
                        AsyncImage(
                            model = uiState.newSelectedImageUri,
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape).border(3.dp, Color.White, CircleShape)
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
                                modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                // ... (End Bagian Gambar) ...

                Spacer(modifier = Modifier.height(32.dp))

                CustomEditProfileField(
                    label = "Nama Lengkap",
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    iconVector = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomEditProfileField(
                    label = "Nama Sekolah",
                    value = uiState.school,
                    onValueChange = viewModel::onSchoolChanged,
                    iconVector = Icons.Default.School
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.saveProfile() },
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF406E)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = "Simpan Perubahan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CustomEditProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    iconVector: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = Color(0xFFE61C5D).copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(2.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold // Font tebal agar terlihat seperti data
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text("Isi $label", color = Color.LightGray)
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Indikator Edit (Kanan) - Menunjukkan ini bisa diedit
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}