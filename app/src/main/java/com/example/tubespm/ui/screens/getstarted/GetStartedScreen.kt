package com.example.tubespm.ui.screens.getstarted

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.R
import com.example.tubespm.ui.theme.TubesPMTheme

// Custom Shape untuk background melengkung
class CurvedTopShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Mulai dari kiri atas
            moveTo(0f, 0f)

            // Garis ke kanan atas
            lineTo(size.width, 0f)

            // Garis ke kanan bawah (sebelum lengkungan)
            lineTo(size.width, size.height - 10f)

            // Lengkungan smooth di bawah (cekung ke bawah)
            cubicTo(
                size.width * 0.75f, size.height - 130f,  // Control point 1
                size.width * 0.35f, size.height - -50f,  // Control point 2
                0f, size.height - 100f                    // End point
            )
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun GetStartedPage(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF0057), Color(0xFFFF7E30))
                    ),
                    shape = CurvedTopShape()
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-100).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(70.dp)
                    )
                    .padding(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logobelut),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(65.dp))
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 60.dp)
        ) {
            Text(
                "Hi, Champions",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF004E)
            )

            Spacer(modifier = Modifier
                .height(12.dp))

            Text(
                "Pelicin jalanmu menuju kampus impian!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Latihan interaktif, analisis skor, dan tryout seru yang bantu kamu siap 100% hadapi UTBK!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF004E)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Text(
                    "Get Started",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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