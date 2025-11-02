package com.example.tubespm.ui.screens.siswa.quiz.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuizBottomNavigation(
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClicked, enabled = isPreviousEnabled) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Soal Sebelumnya")
        }
        IconButton(onClick = onNextClicked, enabled = isNextEnabled) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Soal Selanjutnya")
        }
    }
}