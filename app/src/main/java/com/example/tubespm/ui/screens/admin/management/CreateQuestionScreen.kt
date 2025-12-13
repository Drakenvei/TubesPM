package com.example.tubespm.ui.screens.admin.management

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // State untuk dialog "Add Another Question"
    var showAddAnotherDialog by remember { mutableStateOf(false) }
    var currentQuestionNum by remember { mutableStateOf(questionNumber) }

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
        if (uiState.isSavedSuccess && !showAddAnotherDialog) {
            showAddAnotherDialog = true
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
                        text = "Soal $displayNumber",
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
                            focusedBorderColor = Color.Transparent,
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
                        text = "Tambah Gambar",
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
                        if (uiState.questionImageUri != null) {
                            AsyncImage(
                                model = uiState.questionImageUri,
                                contentDescription = "Question Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (uiState.questionImageUrl != null) {
                            AsyncImage(
                                model = uiState.questionImageUrl,
                                contentDescription = "Question Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
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
                            )
                    ) {
                        options.forEach { option ->
                            val labelStr = option.label.toString()
                            val isCorrect = uiState.correctAnswer == labelStr

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "(${option.label})",
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575),
                                    modifier = Modifier.width(32.dp)
                                )

                                OutlinedTextField(
                                    value = uiState.answerMap[labelStr] ?: "",
                                    onValueChange = { viewModel.updateAnswerOption(labelStr, it) },
                                    modifier = Modifier.weight(1f),
                                    placeholder = {
                                        Text(
                                            option.placeholder,
                                            color = Color(0xFFB0B0B0),
                                            fontSize = 13.sp
                                        )
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
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
                                    )
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Tombol Set Correct Answer
                                Surface(
                                    modifier = Modifier.size(28.dp),
                                    shape = CircleShape,
                                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFD5D5D5)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { viewModel.setCorrectAnswer(labelStr) }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Correct",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // ------------------ PEMBAHASAN ------------------
                    Text(
                        text = "Pembahasan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
        if (showAddAnotherDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddAnotherDialog = false
                    onQuestionCreated()
                },
                title = { Text("Soal Berhasil Ditambahkan", fontWeight = FontWeight.Bold) },
                text = { Text("Apakah Anda ingin menambahkan soal lagi?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showAddAnotherDialog = false

                            // [PERBAIKAN] Update nomor soal
                            currentQuestionNum++

                            // Reset state tapi pertahankan subtestId dan update nomor baru
                            viewModel.resetStateForNextQuestion(currentQuestionNum)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Tambah Soal Lagi")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddAnotherDialog = false
                            onQuestionCreated()
                        }
                    ) {
                        Text("Selesai")
                    }
                }
            )
        }
    }
}