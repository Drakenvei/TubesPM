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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Helper untuk menampilkan gambar Base64
@Composable
fun AdminProfileImage(
    base64String: String,
    isLoading: Boolean,
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
            .size(110.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, Color.White, CircleShape)
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
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

        if(!isLoading){
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(12.dp).background(Color.Green, CircleShape))
        }
    }
}

// --- FUNGSI BARU: DIALOG GANTI PASSWORD (Sudah Diperbaiki) ---
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    isLoading: Boolean
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    val isPasswordValid = newPassword.length >= 6
    val passwordsMatch = newPassword == confirmPassword

    // Perbaikan: Gunakan LaunchedEffect untuk sinkronisasi validasi confirmPassword.
    // Ini menjamin confirmError diperbarui segera setelah newPassword atau confirmPassword berubah.
    LaunchedEffect(newPassword, confirmPassword) {
        confirmError = when {
            confirmPassword.isEmpty() -> null
            !passwordsMatch -> "Password tidak cocok"
            else -> null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Ganti Password", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Input Password Baru
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordError = if (it.length < 6 && it.isNotEmpty()) "Min. 6 karakter" else null
                    },
                    label = { Text("Password Baru") },
                    isError = passwordError != null,
                    supportingText = { if (passwordError != null) Text(passwordError!!, color = MaterialTheme.colorScheme.error) },
                    singleLine = true,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Input Konfirmasi Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        // Validasi sekarang ditangani oleh LaunchedEffect
                    },
                    label = { Text("Konfirmasi Password") },
                    isError = confirmError != null,
                    supportingText = { if (confirmError != null) Text(confirmError!!, color = MaterialTheme.colorScheme.error) },
                    singleLine = true,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isPasswordValid && passwordsMatch) {
                        onConfirm(newPassword, confirmPassword)
                    } else {
                        // Tampilkan error jika tombol ditekan dalam kondisi tidak valid
                        if (!isPasswordValid) passwordError = "Min. 6 karakter"
                        if (!passwordsMatch) confirmError = "Password tidak cocok"
                    }
                },
                enabled = isPasswordValid && passwordsMatch && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Konfirmasi")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}


@Composable
fun AdminProfileScreen(
    paddingValues: PaddingValues,
    onLogoutClick: () -> Unit,
    viewModel: AdminProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // State untuk mengontrol visibilitas dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    // BARU: State untuk dialog ganti password
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateProfilePicture(context, uri)
            }
        }
    )

    // --- LOGIC DIALOG LOGOUT ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(text = "Konfirmasi Logout", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Apakah Anda yakin ingin keluar dari akun ini?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick() // Panggil fungsi logout asli jika user pilih "Ya"
                    }
                ) {
                    Text(text = "Ya, Keluar", color = Color(0xFFFF6F61), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(text = "Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- LOGIC DIALOG GANTI PASSWORD ---
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = {
                showChangePasswordDialog = false
                viewModel.clearError() // Bersihkan error state
            },
            onConfirm = { newPass, _ ->
                viewModel.changePassword(context, newPass)
            },
            isLoading = uiState.isLoading // Gunakan isLoading dari UI State
        )
    }

    // BARU: Menutup dialog jika password berhasil diganti
    LaunchedEffect(uiState.passwordChangeSuccess) {
        if (uiState.passwordChangeSuccess) {
            showChangePasswordDialog = false
            viewModel.clearError() // Membersihkan state sukses setelah dialog ditutup
        }
    }

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
                .offset(y = (-50).dp)
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
                    CustomDivider()
                    // BARU: Ganti Password
                    ProfileInfoRow(
                        label = "Password",
                        value = "Change",
                        isActionable = true,
                        onClick = { showChangePasswordDialog = true } // Tampilkan dialog
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    showLogoutDialog = true
                },
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