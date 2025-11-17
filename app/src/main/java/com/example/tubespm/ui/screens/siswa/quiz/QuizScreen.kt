package com.example.tubespm.ui.screens.siswa.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
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
import com.example.tubespm.data.model.QuizQuestions
import com.example.tubespm.ui.screens.siswa.quiz.components.QuestionNavigator
import com.example.tubespm.ui.screens.siswa.quiz.components.QuizBottomNavigation
import com.example.tubespm.ui.screens.siswa.quiz.components.QuizTopBar
import com.example.tubespm.ui.screens.siswa.quiz.components.AnswerOption
import kotlinx.coroutines.delay

enum class QuizMode {
    TRYOUT,
    LATIHAN
}

@Composable
fun QuizScreen(
    navController: NavHostController,
    activityId: String, //terima ID
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // DITAMBAHKAN: State untuk mengontrol visibilitas dialog konfirmasi
    var showExitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            QuizTopBar(
                mode = uiState.quizMode,
                remainingTimeInSeconds = uiState.remainingTimeInSeconds,
                onBackClicked = { showExitDialog = true },
                onSubmitClicked = {
                    viewModel.submitQuiz()
                    navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Soal tidak ditemukan.")
                }
            } else {
                val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
                if (currentQuestion == null) return@Column // Safety check

                val isFlagged = uiState.flaggedQuestions.contains(currentQuestion.id)

                QuestionNavigator(
                    questionCount = uiState.questions.size,
                    currentIndex = uiState.currentQuestionIndex,
                    // Ubah Set<QuestionID> menjadi Set<Index> untuk UI
                    answeredIndices = uiState.userAnswers.keys.map { qId ->
                        uiState.questions.indexOfFirst { it.id == qId }
                    }.toSet(),
                    flaggedIndices = uiState.flaggedQuestions.map { qId ->
                        uiState.questions.indexOfFirst { it.id == qId }
                    }.toSet(),
                    onQuestionSelected = viewModel::selectQuestion // Kirim event
                )
                Divider()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(currentQuestion.subtestId, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Spacer(Modifier.height(32.dp))
                    Text(currentQuestion.questionText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.weight(1f))

                    currentQuestion.options.forEachIndexed { index, optionText ->
                        val optionLabel = ('A' + index).toString()
                        AnswerOption(
                            optionLabel = optionLabel,
                            optionText = optionText,
                            // --- LOGIKA ISSELECTED BARU ---
                            isSelected = uiState.userAnswers[currentQuestion.id] == optionLabel,
                            onClick = {
                                // --- LOGIKA ONCLICK BARU ---
                                viewModel.onAnswerSelected(currentQuestion, index)
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                viewModel.toggleFlag(currentQuestion.id)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Flag this question ?", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Flag, "Flag", tint = if (isFlagged) Color(0xFFE61C5D) else Color.Gray)
                    }
                }

                QuizBottomNavigation(
                    onPreviousClicked = viewModel::previousQuestion, // <-- Event baru
                    onNextClicked = viewModel::nextQuestion, // <-- Event baru
                    isPreviousEnabled = uiState.currentQuestionIndex > 0,
                    isNextEnabled = uiState.currentQuestionIndex < uiState.questions.size - 1
                )
            }

        }
    }
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                showExitDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }
}

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Keluar dari Sesi?")},
        text = {
            Text("Apakah Anda yakin ingin keluar?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE61C5D))
            ) {
                Text("Ya, Keluar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Batal")
            }
        }
    )
}
