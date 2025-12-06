package com.example.tubespm.ui.screens.siswa.quiz.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

@Composable
fun AnswerOption(
    optionLabel: String,
    optionText: String,
    optionImageBase64: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.LightGray else Color.LightGray.copy(alpha = 0.3f)
    val labelBackgroundColor = if (isSelected) Color(0xFFF8A36B) else Color.White
    val labelTextColor = if (isSelected) Color.White else Color.Black

    val imageBitmap = remember (optionImageBase64) {
        if (!optionImageBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(optionImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else{
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(labelBackgroundColor)
                .border(1.dp, Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = optionLabel, color = labelTextColor, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(16.dp))

        Column (
            modifier = Modifier.weight(1f)
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Option Image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                if (optionText.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (optionText.isNotBlank()) {
                Text(text = optionText, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}