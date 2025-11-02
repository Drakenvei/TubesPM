package com.example.tubespm.ui.screens.getstarted

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.ui.theme.TubesPMTheme

@Composable
fun GetStartedPage(onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFFF6F6))) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(100.dp))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFF0057), Color(0xFFFF7E30))),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Hi, Champions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque vel.",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF004E))
                ) {
                    Text("Get Started", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GetStartedPagePreview() {
    TubesPMTheme {
        GetStartedPage(onClick = {})
    }
}