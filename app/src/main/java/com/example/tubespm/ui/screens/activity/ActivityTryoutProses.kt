package com.example.tubespm.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.data.model.InProgressSectionState
import com.example.tubespm.data.model.TryoutInProgress

@Composable
fun TryoutDalamProsesContent(
    tryouts: List<TryoutInProgress>,
    onContinueClick: () -> Unit
) {
    LazyColumn (
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tryouts) { tryout ->
            TryoutInProgressCard(
                tryout = tryout,
                onContinueClick = onContinueClick
            )
        }
    }
}

@Composable
fun TryoutInProgressCard(
    tryout: TryoutInProgress,
    onContinueClick: () -> Unit
) {
    Card (
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column (
            Modifier.padding(16.dp)
        ) {
            Text(
                tryout.title,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Column (
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tryout.sections.forEach { section ->
                    InProgressSection(
                        section = section,
                        onContinueClick = onContinueClick
                    )
                    if (tryout.sections.last() != section) {
                        Divider(
                            color = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InProgressSection(
    section: InProgressSectionState,
    onContinueClick: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                section.title,
                color = Color(0xFFE61C5D),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(12.dp))

        if (section.isLocked) {
            Row (
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoRow(
                    icon = Icons.Default.Description,
                    text = "${section.totalQuestions} soal"
                )
                InfoRow(
                    icon = Icons.Default.Timer,
                    text = "90 menit"
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Color.Green.copy(alpha = 0.5f),
                    disabledContentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Menunggu subtest sebelumnya")
            }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoRow(
                    icon = Icons.Default.Description,
                    text = "${section.progress}/${section.totalQuestions} soal"
                )
                InfoRow(
                    icon = Icons.Default.Timer,
                    text = "Sisa waktu: ${section.remainingTime}"
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { section.progress.toFloat() / section.totalQuestions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onContinueClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    "Lanjutkan Tryout",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
