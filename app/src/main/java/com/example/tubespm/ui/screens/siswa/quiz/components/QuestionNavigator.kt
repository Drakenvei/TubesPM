package com.example.tubespm.ui.screens.siswa.quiz.components

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

@Composable
fun QuestionNavigator(
    questionCount: Int,
    currentIndex: Int,
    answeredIndices: Set<Int>,
    flaggedIndices: Set<Int>,
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
            val isAnswered = answeredIndices.contains(index)
            val isFlagged = flaggedIndices.contains(index)

            val backgroundColor = when {
                isCurrent -> Color(0xFFF8A36B).copy(alpha = 0.3f)
                isFlagged -> Color(0xFFE61C5D).copy(alpha = 0.2f)
                isAnswered -> Color.LightGray.copy(alpha = 0.5f)
                else -> Color.Transparent
            }

            val borderColor = when {
                isCurrent -> Color(0xFFF8A36B)
                isFlagged -> Color(0xFFE61C5D)
                else -> Color.LightGray
            }

            val textColor = if (isCurrent) Color(0xFFF8A36B) else Color.Black

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
                Text(text = numberString, color = textColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}