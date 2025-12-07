package com.example.tubespm.ui.screens.admin.management

import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.PaddingValues
import coil.compose.AsyncImage

data class AnswerOption(
    val label: Char,
    val placeholder: String = "Add Here"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionScreen(
    tryoutId: String,           // <-- Perlu ID Tryout/Latihan untuk database
    questionId: String,         // <-- Perlu ID Soal untuk database
    paketName: String,
    questionNumber: Int,
    paddingValuesFromNavHost: PaddingValues,
    onBackClick: () -> Unit,
    type: String = "tryout",    // <-- "tryout" atau "latihan_soal"
    // Inject ViewModel
    viewModel: EditQuestionViewModel = viewModel()
) {
    // Load data saat pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.loadQuestion(tryoutId, questionId, type)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Handle efek setelah simpan sukses
    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            onBackClick() // Kembali ke layar sebelumnya jika sukses simpan
        }
    }

    val options = remember {
        listOf(AnswerOption('A'), AnswerOption('B'), AnswerOption('C'), AnswerOption('D'), AnswerOption('E'))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Edit Question", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = paketName, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Tombol Save
                    IconButton(onClick = { viewModel.saveQuestion(tryoutId, questionId, type) }) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF9966), titleContentColor = Color.White)
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF9966))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {

                    Text(text = "Question $questionNumber", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                    Spacer(modifier = Modifier.height(16.dp))

                    // ------------------ TULIS SOAL ------------------
                    Text(text = "Tulis Soal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = uiState.questionData.questionText,
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
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ------------------ TAMBAH GAMBAR ------------------
                    Text(text = "Tambah Gambar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let { viewModel.updateQuestionImageUri(it) }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(6.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            uiState.questionImageUri != null -> {
                                AsyncImage(
                                    model = uiState.questionImageUri,
                                    contentDescription = "Gambar Soal",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            uiState.questionData.questionImage != null -> {
                                AsyncImage(
                                    model = uiState.questionData.questionImage,
                                    contentDescription = "Gambar Soal",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Icon(Icons.Default.Add, contentDescription = "Tambah Gambar", tint = Color(0xFF9E9E9E), modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ------------------ PILIHAN JAWABAN ------------------
                    Text(text = "Pilihan Jawaban", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))) {
                        options.forEach { option ->
                            val labelStr = option.label.toString()
                            val isCorrect = uiState.questionData.correctAnswer == labelStr

                            Row(
                                modifier = Modifier.fillMaxWidth().height(42.dp).padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "(${option.label})", fontSize = 13.sp, color = Color(0xFF757575), modifier = Modifier.width(32.dp))

                                OutlinedTextField(
                                    value = uiState.answerMap[labelStr] ?: "",
                                    onValueChange = { viewModel.updateAnswerOption(labelStr, it) },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    placeholder = { Text(option.placeholder, color = Color(0xFFB0B0B0), fontSize = 13.sp) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFE0E0E0),
                                        unfocusedContainerColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = Color.Black
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
                                        modifier = Modifier.fillMaxSize().noRippleClickable {
                                            viewModel.setCorrectAnswer(labelStr)
                                        },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Correct", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            if (option.label != 'E') Divider(color = Color(0xFFCCCCCC), thickness = 0.5.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ------------------ PEMBAHASAN ------------------
                    Text(text = "Pembahasan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.questionData.discussion,
                        onValueChange = { viewModel.updateDiscussionText(it) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Tulis Pembahasan Di Sini", color = Color(0xFF9E9E9E)) },
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
                }
            }
        }
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
}