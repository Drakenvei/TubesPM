package com.example.tubespm.ui.screens.pembahasan

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.tubespm.data.model.QuestionWithExplanation
import com.example.tubespm.ui.screens.pembahasan.components.PembahasanAnswerOption
import com.example.tubespm.ui.screens.pembahasan.components.PembahasanNavigator
import com.example.tubespm.ui.screens.siswa.pembahasan.PembahasanUiState
import com.example.tubespm.ui.screens.siswa.pembahasan.PembahasanViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembahasanScreen(
    navController: NavHostController,
    viewModel: PembahasanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val currentQuestionForTopBar = uiState.questions.getOrNull(currentQuestionIndex)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pembahasan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (currentQuestionForTopBar != null) {
                            Text(
                                text = currentQuestionForTopBar.subtest,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }

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
                        // Question Image
                        val questionBitmap = remember (currentQuestion.questionImage) {
                            if (!currentQuestion.questionImage.isNullOrBlank()) {
                                try {
                                    val bytes = Base64.decode(currentQuestion.questionImage, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            } else {
                                null
                            }
                        }

                        if (questionBitmap != null) {
                            Image(
                                bitmap = questionBitmap,
                                contentDescription = "Gambar Soal",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Question Text
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 28.sp
                            ),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Answer Options
                        currentQuestion.options.forEachIndexed { index, optionText ->
                            val optionImg = currentQuestion.optionImages.getOrNull(index)
                            PembahasanAnswerOption(
                                optionLabel = ('A' + index).toString(),
                                optionText = optionText,
                                optionImageBase64 = optionImg,
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

                                Button(
                                    onClick = {
                                        // 1. Ambil activityId dari uiState
                                        val activityId = uiState.activityId

                                        // 2. Ambil currentQuestionIndex
                                        val questionIndex = currentQuestionIndex

                                        // FIX: Navigasi HANYA JIKA activityId TIDAK KOSONG
                                        if (activityId.isNotBlank()) {
                                            navController.navigate("diskusi_ai/$activityId/$questionIndex")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AddComment, // Ikon untuk diskusi/chat
                                        contentDescription = "Tanya AI",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tanya AI Mengenai Soal Ini")
                                }
                                // --- AKHIR TOMBOL TANYA AI ---

                                Spacer(modifier = Modifier.height(16.dp))

                                val explanationBitmap = remember (currentQuestion.explanationImage) {
                                    if (!currentQuestion.explanationImage.isNullOrBlank()) {
                                        try {
                                            val bytes = Base64.decode(currentQuestion.explanationImage, Base64.DEFAULT)
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } else {
                                        null
                                    }
                                }

                                if (explanationBitmap != null) {
                                    Image(
                                        bitmap = explanationBitmap,
                                        contentDescription = "Gambar Pembahasan",
                                        contentScale = ContentScale.FillWidth,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

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