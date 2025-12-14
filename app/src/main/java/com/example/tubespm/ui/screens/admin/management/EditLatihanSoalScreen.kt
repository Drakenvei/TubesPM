package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLatihanSoalScreen(
    latihanId: String,
    paddingValuesFromNavHost: androidx.compose.foundation.layout.PaddingValues,
    onBackClick: () -> Unit,
    onLatihanUpdated: () -> Unit,
    onGoToListSoal: (String, String, String, String) -> Unit = { _, _, _, _ -> }, // (type, parentId, paketName, subtestId)
    viewModel: EditLatihanSoalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // [BARU] Logika Kunci: Hanya bisa edit jika inactive
    val isEditable = uiState.status != "active"

    // State Dropdown
    var expandedSubtest by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val subtestOptions = listOf(
        "Penalaran Umum",
        "Pengetahuan Kuantitatif",
        "Pengetahuan dan Pemahaman Umum",
        "Pemahaman Bacaan dan Menulis",
        "Literasi dalam Bahasa Indonesia",
        "Literasi dalam Bahasa Inggris",
        "Penalaran Matematika"
    )

    LaunchedEffect(latihanId) {
        viewModel.loadLatihanSoal(latihanId)
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Latihan Soal",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE61C5D),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(paddingValuesFromNavHost)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFE61C5D))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Kode
                    Text(
                        text = "Kode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.code,
                        onValueChange = { viewModel.updateCode(it) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditable,
                        placeholder = { Text("Contoh: LAT-ALG-01", color = Color(0xFF9E9E9E)) },
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121),
                            cursorColor = Color(0xFF212121)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Judul
                    Text(
                        text = "Judul",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditable,
                        placeholder = { Text("Contoh: Latihan Aljabar", color = Color(0xFF9E9E9E)) },
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121),
                            cursorColor = Color(0xFF212121)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtest
                    Text(
                        text = "Subtest",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedSubtest && isEditable,
                        onExpandedChange = { if (isEditable) expandedSubtest = !expandedSubtest }
                    ) {
                        OutlinedTextField(
                            value = uiState.subtest,
                            onValueChange = {},
                            readOnly = true,
                            enabled = isEditable,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            placeholder = { Text("Pilih Subtest", color = Color(0xFF9E9E9E)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubtest) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color(0xFF212121),
                                unfocusedTextColor = Color(0xFF212121)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSubtest,
                            onDismissRequest = { expandedSubtest = false }
                        ) {
                            subtestOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.updateSubtest(option)
                                        expandedSubtest = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kisi-kisi
                    Text("Kisi-kisi (Pisahkan dengan koma)", fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.topicsString,
                        onValueChange = { viewModel.updateTopicsString(it) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditable,
                        placeholder = { Text("Contoh: Aljabar, Geometri") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(6.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status
                    Button(
                        onClick = {
                            // Toggle status di UI State
                            viewModel.toggleLatihanStatus(latihanId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isStatusUpdating,
                        colors = ButtonDefaults.buttonColors(
                            // Merah jika mau Nonaktifkan (sedang active), Hijau jika mau Aktifkan (sedang inactive)
                            containerColor = if (uiState.status == "active") Color(0xFFE53935) else Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.isStatusUpdating) {
                            // Tampilkan loading kecil di dalam tombol status jika sedang proses
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (uiState.status == "active") "Nonaktifkan Latihan" else "Aktifkan Latihan",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    uiState.error?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFE53935),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Tombol Simpan
                    Button(
                        onClick = {
                            viewModel.saveLatihanSoal(
                                latihanId = latihanId,
                                onSuccess = { },
                                onError = { error ->
                                    android.widget.Toast.makeText(
                                        context,
                                        error,
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !uiState.isSaving && isEditable,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Simpan Perubahan",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Pesan peringatan jika terkunci
                    if (!isEditable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "*Nonaktifkan latihan soal terlebih dahulu untuk mengedit data.",
                            color = Color(0xFFE53935),
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
        
        // Dialog setelah berhasil update
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("Berhasil", fontWeight = FontWeight.Bold) },
                text = { Text("Latihan soal berhasil diperbarui. Apakah Anda ingin mengelola soal?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            // Pass nama latihan dari state
                            val latihanName = uiState.title.ifEmpty { "Latihan Soal" }
                            onGoToListSoal("latihan_soal", latihanId, latihanName, uiState.subtestId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Kelola Soal")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            onLatihanUpdated()
                        }
                    ) {
                        Text("Kembali")
                    }
                }
            )
        }
    }
}



