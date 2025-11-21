package com.example.tubespm.ui.screens.siswa.quiz.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.tubespm.ui.screens.siswa.quiz.QuizMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTopBar(
    mode: QuizMode,
    title: String,
    remainingTimeInSeconds: Long,
    onBackClicked: () -> Unit,
    onSubmitClicked: () -> Unit,
    isLastSubtest: Boolean
) {
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d : %02d : %02d", hours, minutes, secs)
    }

    TopAppBar(
        title = {
            Column (horizontalAlignment = Alignment.CenterHorizontally) {
                // Tampilkan Timer
                if (mode == QuizMode.TRYOUT) {
                    Text(
                        text = formatTime(remainingTimeInSeconds),
                        fontWeight = FontWeight.Bold,
                        color = if (remainingTimeInSeconds < 300) Color.Red else Color.Black
                    )
                }
                // Tampilkan Nama Subtest
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
        },
        actions = {
            Button(
                onClick = onSubmitClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastSubtest) Color(0xFF30D158) else Color(0xFFE61C5D)
                )
            ) {
                Text(if (isLastSubtest) "Selesai" else "Lanjut Subtes")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}