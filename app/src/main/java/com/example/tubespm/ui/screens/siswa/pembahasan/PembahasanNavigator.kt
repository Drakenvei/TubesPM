package com.example.tubespm.ui.screens.pembahasan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tubespm.data.model.QuestionWithExplanation

@Composable
fun PembahasanNavigator(
    questionCount: Int,
    currentIndex: Int,
    questions: List<QuestionWithExplanation>,
    onQuestionSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(questionCount) { index ->
            val numberString = (index + 1).toString().padStart(2, '0')
            val isCurrent = index == currentIndex
            val question = questions[index]

            // Determine if user answered correctly, wrongly, or didn't answer
            val isCorrect = question.userAnswerIndex == question.correctAnswerIndex
            val isWrong = question.userAnswerIndex != null && !isCorrect
            val isUnanswered = question.userAnswerIndex == null

            val backgroundColor = when {
                isCurrent -> Color(0xFFF8A36B).copy(alpha = 0.3f)
                isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                isWrong -> Color(0xFFE61C5D).copy(alpha = 0.2f)
                isUnanswered -> Color.LightGray.copy(alpha = 0.3f)
                else -> Color.Transparent
            }

            val borderColor = when {
                isCurrent -> Color(0xFFF8A36B)
                isCorrect -> Color(0xFF4CAF50)
                isWrong -> Color(0xFFE61C5D)
                else -> Color.LightGray
            }

            val textColor = when {
                isCurrent -> Color(0xFFF8A36B)
                isCorrect -> Color(0xFF4CAF50)
                isWrong -> Color(0xFFE61C5D)
                else -> Color.Black
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(2.dp, borderColor, CircleShape)
                    .background(backgroundColor)
                    .clickable { onQuestionSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numberString,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}