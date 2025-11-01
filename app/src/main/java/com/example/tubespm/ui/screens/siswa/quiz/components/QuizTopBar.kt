package com.example.tubespm.ui.screens.siswa.quiz.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.tubespm.ui.screens.quiz.QuizMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTopBar(
    mode: QuizMode,
    remainingTimeInSeconds: Long,
    onBackClicked: () -> Unit,
    onSubmitClicked: () -> Unit
) {
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d : %02d : %02d", hours, minutes, secs)
    }

    TopAppBar(
        title = {
            if (mode == QuizMode.TRYOUT) {
                Text(
                    text = formatTime(remainingTimeInSeconds),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
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
                    containerColor = Color.LightGray.copy(alpha = 0.5f),
                    contentColor = Color.Black
                )
            ) {
                Text("Submit")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}