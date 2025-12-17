package com.example.tubespm.ui.screens.admin.management

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.tubespm.di.AppModule
import com.example.tubespm.repository.ExerciseCatalogRepository
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuestionScreen(
    parentId: String,           // tryoutId atau latihanId
    type: String,                // "tryout" atau "latihan_soal"
    paketName: String,
    questionNumber: Int = 1,      // Ini ID Database (misal 5)
    displayNumber: Int = 1,       // [BARU] Ini ID Visual (misal 4)
    subtestId: String? = null,   // SubtestId untuk section tryout (optional)
    targetCount: Int = 0,
    paddingValuesFromNavHost: androidx.compose.foundation.layout.PaddingValues,
    onBackClick: () -> Unit,
    onQuestionCreated: () -> Unit, // Callback setelah soal berhasil dibuat
    viewModel: CreateQuestionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val repository: ExerciseCatalogRepository = AppModule.provideExerciseCatalogRepository(
                    FirebaseFirestore.getInstance()
                )
                @Suppress("UNCHECKED_CAST")
                return CreateQuestionViewModel(repository) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Gunakan Mutable State untuk nomor agar bisa di-update real-time
    var currentDbNum by remember { mutableIntStateOf(questionNumber) }
    var currentDisplayNum by remember { mutableIntStateOf(displayNumber) }

    var showSuccessDialog by remember { mutableStateOf(false) }

    // Inisialisasi Data ViewModel saat pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.initData(
            questionNumber = questionNumber,
            subtestId = subtestId
        )
    }

    val options = remember {
        listOf(
            AnswerOption('A'),
            AnswerOption('B'),
            AnswerOption('C'),
            AnswerOption('D'),
            AnswerOption('E')
        )
    }

    // Handle success
    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess && !showSuccessDialog) {
            showSuccessDialog = true
        }
    }

    // Set initial question number and subtestId
//    LaunchedEffect(subtestId) {
//        // Log untuk debugging (Cek di Logcat)
//        android.util.Log.d("CreateQuestion", "Received SubtestID: $subtestId")
//
//        if (!subtestId.isNullOrBlank()) {
//            viewModel.updateSubtestId(subtestId)
//        }
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tambah Soal Baru",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = paketName,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Tombol Save
                    IconButton(
                        onClick = {
                            viewModel.createQuestion(
                                context = context,
                                parentId = parentId,
                                type = type,
                                onSuccess = {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Soal berhasil ditambahkan",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onError = { error ->
                                    android.widget.Toast.makeText(
                                        context,
                                        error,
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Save",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9966),
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
                    CircularProgressIndicator(color = Color(0xFFFF9966))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Soal $currentDisplayNum",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ------------------ INPUT QUESTION ------------------
                    Text(
                        text = "Tulis Soal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = uiState.questionText,
                        onValueChange = { viewModel.updateQuestionText(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tulis Soal Di Sini", color = Color(0xFF9E9E9E)) },
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFFFF9966),
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121),
                            cursorColor = Color(0xFF212121)
                        ),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ------------------ TAMBAH GAMBAR ------------------
                    Text(
                        text = "Tambah Gambar Soal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let { viewModel.updateQuestionImageUri(it) }
                    }

                    if (uiState.questionImageUri != null) {
                        ImagePreviewWithDelete(
                            uri = uiState.questionImageUri,
                            onDelete = { viewModel.updateQuestionImageUri(null) }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    Color(0xFFE0E0E0),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    Color(0xFFBDBDBD),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Picture",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ------------------ PILIHAN JAWABAN ------------------
                    Text(
                        text = "Pilihan Jawaban",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFE0E0E0),
                                RoundedCornerShape(6.dp)
                            ).padding(vertical = 4.dp)
                    ) {
                        options.forEach { option ->
                            val labelStr = option.label.toString()
                            val isCorrect = uiState.correctAnswer == labelStr
                            val currentOptionUri = uiState.optionImageUris[labelStr]

                            // State untuk mengetahui tombol mana yang diklik agar launcher tau
                            val optionImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                                uri?.let { viewModel.updateOptionImageUri(labelStr, it) }
                            }

                            Column (
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "(${option.label})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF757575),
                                        modifier = Modifier.width(32.dp)
                                    )

                                    OutlinedTextField(
                                        value = uiState.answerMap[labelStr] ?: "",
                                        onValueChange = { viewModel.updateAnswerOption(labelStr, it) },
                                        modifier = Modifier.weight(1f),
                                        placeholder = {
                                            Text(
                                                if (currentOptionUri != null) "Teks (Opsional)" else option.placeholder,
                                                color = Color(0xFFB0B0B0),
                                                fontSize = 13.sp
                                            )
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            color = Color(0xFF212121)
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedTextColor = Color(0xFF212121),
                                            unfocusedTextColor = Color(0xFF212121),
                                            cursorColor = Color(0xFF212121)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Tombol Tambah Gambar (Kecil)
                                    IconButton(
                                        onClick = { optionImageLauncher.launch("image/*") },
                                        modifier = Modifier.size(36.dp).background(if(currentOptionUri != null) Color(0xFFBBDEFB) else Color.White, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = "Add Image", tint = if(currentOptionUri != null) Color(0xFF1976D2) else Color.Gray, modifier = Modifier.size(20.dp))
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Tombol Centang Jawaban Benar
                                    IconButton(
                                        onClick = { viewModel.setCorrectAnswer(labelStr) },
                                        modifier = Modifier.size(36.dp).background(if (isCorrect) Color(0xFF4CAF50) else Color.White, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Correct", tint = if (isCorrect) Color.White else Color.LightGray, modifier = Modifier.size(20.dp))
                                    }
                                }
                                // Preview Gambar Opsi (Di Bawah)
                                if (currentOptionUri != null) {
                                    Box(modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)) {
                                        ImagePreviewWithDelete(
                                            uri = currentOptionUri,
                                            onDelete = { viewModel.updateOptionImageUri(labelStr, null) },
                                            size = 100.dp
                                        )
                                    }
                                }
                                if (option.label != 'E') {
                                    HorizontalDivider(
                                        color = Color(0xFFCCCCCC),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ------------------ PEMBAHASAN ------------------
                    Text(
                        text = "Pembahasan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val explanationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        uri?.let { viewModel.updateExplanationImageUri(it) }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                            .padding(1.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.discussion,
                            onValueChange = { viewModel.updateDiscussionText(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Tulis Pembahasan Di Sini", color = Color(0xFF9E9E9E)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color(0xFFE0E0E0),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color(0xFF212121),
                                unfocusedTextColor = Color(0xFF212121),
                                cursorColor = Color(0xFF212121)
                            ),
                            minLines = 4
                        )

                        // [BARU] Tombol Add Image Pembahasan
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFE0E0E0),
                                    RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp).clickable { explanationLauncher.launch("image/*") },
                                shape = RoundedCornerShape(4.dp),
                                color = if (uiState.explanationImageUri != null) Color(0xFFBBDEFB) else Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Image", tint = Color.Gray)
                                }
                            }
                        }
                    }

                    // [BARU] Preview Gambar Pembahasan
                    if (uiState.explanationImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ImagePreviewWithDelete(
                            uri = uiState.explanationImageUri,
                            onDelete = { viewModel.updateExplanationImageUri(null) },
                            size = 150.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                    // Error message
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = Color(0xFFE53935),
                            fontSize = 12.sp
                        )
                    }

                }
            }
        }

        // Dialog untuk menambah soal lagi
        if (showSuccessDialog) {
            // Cek apakah target sudah tercapai?
            val isTargetReached = targetCount > 0 && currentDisplayNum >= targetCount
            AlertDialog(
                onDismissRequest = {
//                    showSuccessDialog = false
//                    onQuestionCreated()
                },
                title = { Text(if (isTargetReached) "Target Tercapai!" else "Berhasil Ditambahkan", fontWeight = FontWeight.Bold) },
                text = {
                    if (isTargetReached) Text("Anda telah mencapai target $targetCount soal. Silakan kembali.")
                    else Text("Apakah Anda ingin menambahkan soal lagi?")
                },
                confirmButton = {
                    if (!isTargetReached) {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                // [PENTING] Increment nomor soal
                                currentDbNum++
                                currentDisplayNum++
                                viewModel.resetStateForNextQuestion(currentDbNum)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) { Text("Tambah Lagi") }
                    } else {
                        Button(onClick = { showSuccessDialog = false; onQuestionCreated() }) { Text("Selesai") }
                    }
                },
                dismissButton = {
                    if (!isTargetReached) {
                        TextButton(onClick = { showSuccessDialog = false; onQuestionCreated() }) { Text("Selesai") }
                    }
                }
            )
        }
    }
}

@Composable
fun ImagePreviewWithDelete(
    uri: Uri?,
    onDelete: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 120.dp
) {
    if (uri == null) return

    Box(
        modifier = Modifier.size(size)
    ) {
        // 1. Gambar (Layer Belakang)
        // Diberi padding agar tombol X memiliki ruang dan tidak terpotong layout parent
        AsyncImage(
            model = uri,
            contentDescription = "Preview Image",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp, end = 6.dp) // Geser gambar sedikit ke kiri-bawah
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        // 2. Tombol X (Layer Depan)
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd) // Pojok kanan atas
                .size(20.dp)
                .background(Color.Red, CircleShape)
                .zIndex(1f) // Memastikan tombol selalu di lapisan paling atas
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete Image",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}