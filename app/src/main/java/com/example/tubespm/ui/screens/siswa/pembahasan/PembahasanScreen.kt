package com.example.tubespm.ui.screens.pembahasan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.tubespm.data.model.QuestionWithExplanation
import com.example.tubespm.ui.screens.pembahasan.components.PembahasanAnswerOption
import com.example.tubespm.ui.screens.pembahasan.components.PembahasanNavigator
import com.example.tubespm.ui.screens.siswa.pembahasan.PembahasanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembahasanScreen(
    navController: NavHostController,
    viewModel: PembahasanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentQuestionIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pembahasan",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Spacer untuk menjaga judul tetap di tengah
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    }
                }
                uiState.questions.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Data pembahasan tidak ditemukan.")
                    }
                }
                else -> {
                    val questions = uiState.questions
                    val currentQuestion = questions[currentQuestionIndex]

                    // Question Navigator
                    PembahasanNavigator(
                        questionCount = questions.size,
                        currentIndex = currentQuestionIndex,
                        questions = questions,
                        onQuestionSelected = { index -> currentQuestionIndex = index }
                    )

                    Divider()

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Subtest
                        Text(
                            text = currentQuestion.subtest,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Question Text
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Answer Options
                        currentQuestion.options.forEachIndexed { index, optionText ->
                            PembahasanAnswerOption(
                                optionLabel = ('A' + index).toString(),
                                optionText = optionText,
                                isCorrectAnswer = index == currentQuestion.correctAnswerIndex,
                                isUserAnswer = index == currentQuestion.userAnswerIndex
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Pembahasan Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE0D5F0)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Pembahasan:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentQuestion.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Bottom Navigation
                    Divider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                            enabled = currentQuestionIndex > 0
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Soal Sebelumnya"
                            )
                        }

                        Text(
                            text = "${currentQuestionIndex + 1} / ${questions.size}",
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { if (currentQuestionIndex < questions.size - 1) currentQuestionIndex++ },
                            enabled = currentQuestionIndex < questions.size - 1
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Soal Selanjutnya"
                            )
                        }
                    }
                }
            }
        }
    }
}