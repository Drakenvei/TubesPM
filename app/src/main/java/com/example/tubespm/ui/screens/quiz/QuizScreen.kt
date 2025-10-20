package com.example.tubespm.ui.screens.quiz

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.ui.screens.quiz.components.AnswerOption
import com.example.tubespm.ui.screens.quiz.components.QuestionNavigator
import com.example.tubespm.ui.screens.quiz.components.QuizBottomNavigation
import com.example.tubespm.ui.screens.quiz.components.QuizTopBar
import kotlinx.coroutines.delay

enum class QuizMode {
    TRYOUT,
    LATIHAN
}

@Composable
fun QuizScreen(
    navController: NavHostController,
    questions: List<QuizQuestion>,
    mode: QuizMode,
    onSessionFinished: (Map<Int, Int>) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }
    val flaggedQuestions = remember { mutableStateSetOf<Int>() }

    // DITAMBAHKAN: State untuk mengontrol visibilitas dialog konfirmasi
    var showExitDialog by remember { mutableStateOf(false) }

    var remainingTimeInSeconds by remember { mutableStateOf(3600L) }

    if (mode == QuizMode.TRYOUT) {
        LaunchedEffect(key1 = remainingTimeInSeconds) {
            if (remainingTimeInSeconds > 0) {
                delay(1000L)
                remainingTimeInSeconds--
            } else {
                onSessionFinished(userAnswers)
            }
        }
    }

    val currentQuestion = questions[currentQuestionIndex]
    val isFlagged = flaggedQuestions.contains(currentQuestion.id)

    Scaffold(
        topBar = {
            QuizTopBar(
                mode = mode,
                remainingTimeInSeconds = remainingTimeInSeconds,
                onBackClicked = { showExitDialog = true },
                onSubmitClicked = { onSessionFinished(userAnswers) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            QuestionNavigator(
                questionCount = questions.size,
                currentIndex = currentQuestionIndex,
                answeredIndices = userAnswers.keys.map { qId -> questions.indexOfFirst { it.id == qId } }.toSet(),
                flaggedIndices = flaggedQuestions.map { qId -> questions.indexOfFirst { it.id == qId } }.toSet(),
                onQuestionSelected = { index -> currentQuestionIndex = index }
            )
            Divider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(currentQuestion.subtest, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(Modifier.height(32.dp))
                Text(currentQuestion.questionText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.weight(1f))

                currentQuestion.options.forEachIndexed { index, optionText ->
                    AnswerOption(
                        optionLabel = ('A' + index).toString(),
                        optionText = optionText,
                        isSelected = userAnswers[currentQuestion.id] == index,
                        onClick = { userAnswers[currentQuestion.id] = index }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            if (isFlagged) flaggedQuestions.remove(currentQuestion.id)
                            else flaggedQuestions.add(currentQuestion.id)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Flag this question ?", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Flag, "Flag", tint = if (isFlagged) Color(0xFFE61C5D) else Color.Gray)
                }
            }

            QuizBottomNavigation(
                onPreviousClicked = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                onNextClicked = { if (currentQuestionIndex < questions.size - 1) currentQuestionIndex++ },
                isPreviousEnabled = currentQuestionIndex > 0,
                isNextEnabled = currentQuestionIndex < questions.size - 1
            )
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
