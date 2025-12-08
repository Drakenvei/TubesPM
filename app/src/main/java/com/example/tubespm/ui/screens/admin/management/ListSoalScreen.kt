package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.FabPosition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.tubespm.ui.theme.TubesPMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSoalScreen(
    parentId: String,
    type: String,
    sectionName: String? = null,
    subtestId: String? = null,
    sectionId: String? = null,
    paketName: String,
    onBackClick: () -> Unit,
    onEditQuestion: (String, String, String, String, Int) -> Unit,
    onAddQuestion: (String, String, String, Int, String?) -> Unit,
    viewModel: ListSoalViewModel = viewModel()
) {
    // Extract section name dari paketName jika format "Paket - Section"
    val actualSectionName = remember(paketName) {
        if (paketName.contains(" - ")) {
            paketName.split(" - ").getOrNull(1) ?: sectionName
        } else {
            sectionName
        }
    }
    
    // Extract actual paket name (tanpa section)
    val actualPaketName = remember(paketName) {
        if (paketName.contains(" - ")) {
            paketName.split(" - ").first()
        } else {
            paketName
        }
    }
    
    // Load questions saat screen pertama kali dibuka
    LaunchedEffect(parentId, type, subtestId, sectionId, actualSectionName) {
        when {
            sectionId != null || actualSectionName != null -> {
                // Gunakan sectionId jika ada, atau sectionName sebagai fallback
                val identifier = sectionId ?: actualSectionName ?: ""
                if (identifier.isNotEmpty()) {
                    viewModel.loadQuestionsBySection(parentId, type, identifier)
                } else {
                    viewModel.loadQuestions(parentId, type)
                }
            }
            subtestId != null -> {
                viewModel.loadQuestionsBySubtest(parentId, type, subtestId)
            }
            else -> {
                viewModel.loadQuestions(parentId, type)
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (actualSectionName != null) "Soal - $actualSectionName" else "Daftar Soal",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = actualPaketName,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9966),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val nextQuestionNumber = (uiState.questions.maxOfOrNull { it.questionNumber } ?: 0) + 1
                    val subtestIdForNewQuestion = if (type == "tryout") uiState.currentSubtestId else null
                    onAddQuestion(type, parentId, paketName, nextQuestionNumber, subtestIdForNewQuestion)
                },
                modifier = Modifier.offset(x = 0.dp, y = (-80).dp),
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Soal")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                // HAPUS .padding(paddingValuesFromNavHost)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF9966))
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "Terjadi kesalahan",
                                color = Color(0xFFE53935),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                if (subtestId != null) {
                                    viewModel.loadQuestionsBySubtest(parentId, type, subtestId)
                                } else {
                                    viewModel.loadQuestions(parentId, type)
                                }
                            }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                uiState.questions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Belum ada soal",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tekan tombol + untuk menambah soal",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.questions) { question ->
                            QuestionCard(
                                question = question,
                                onClick = {
                                    onEditQuestion(
                                        type,
                                        parentId,
                                        question.id,
                                        paketName,
                                        question.questionNumber
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: com.example.tubespm.data.model.QuizQuestion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Soal ${question.questionNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.questionText.take(100) + if (question.questionText.length > 100) "..." else "",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    maxLines = 2
                )
            }
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color(0xFF757575),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

