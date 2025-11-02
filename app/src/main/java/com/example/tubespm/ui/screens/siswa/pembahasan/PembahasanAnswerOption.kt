package com.example.tubespm.ui.screens.pembahasan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PembahasanAnswerOption(
    optionLabel: String,
    optionText: String,
    isCorrectAnswer: Boolean,
    isUserAnswer: Boolean
) {
    // Determine colors based on answer status
    val backgroundColor = when {
        isCorrectAnswer -> Color(0xFF4CAF50).copy(alpha = 0.1f) // Light green for correct
        isUserAnswer && !isCorrectAnswer -> Color(0xFFE61C5D).copy(alpha = 0.1f) // Light red for wrong user answer
        else -> Color.LightGray.copy(alpha = 0.3f)
    }

    val borderColor = when {
        isCorrectAnswer -> Color(0xFF4CAF50) // Green border for correct
        isUserAnswer && !isCorrectAnswer -> Color(0xFFE61C5D) // Red border for wrong user answer
        else -> Color.Transparent
    }

    val labelBackgroundColor = when {
        isCorrectAnswer -> Color(0xFF4CAF50) // Green for correct answer
        isUserAnswer && !isCorrectAnswer -> Color(0xFFE61C5D) // Red for wrong user answer
        else -> Color.White
    }

    val labelTextColor = when {
        isCorrectAnswer || (isUserAnswer && !isCorrectAnswer) -> Color.White
        else -> Color.Black
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (borderColor != Color.Transparent) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Option Label (A, B, C, D, E)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(labelBackgroundColor)
                .border(1.dp, Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = optionLabel,
                color = labelTextColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(16.dp))

        // Option Text
        Text(
            text = optionText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        // Icon indicator
        if (isCorrectAnswer) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Jawaban Benar",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
        } else if (isUserAnswer && !isCorrectAnswer) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Jawaban Salah",
                tint = Color(0xFFE61C5D),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}