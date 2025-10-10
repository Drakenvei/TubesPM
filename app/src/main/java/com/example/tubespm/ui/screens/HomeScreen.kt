package com.example.tubespm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.R
import com.example.tubespm.ui.theme.TubesPMTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("User") }

    // Fetch user data when HomeScreen loads
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    if (name != null) {
                        userName = name
                    }
                }
        }
    }

    Scaffold(
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { /* TODO: Add Tryout */ },
//                containerColor = Color.White,
//                shape = CircleShape,
//                modifier = Modifier.size(64.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Add",
//                    tint = Color(0xFFE91E63),
//                    modifier = Modifier.size(36.dp)
//                )
//            }
//        },
//        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE91E63))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Hi, $userName",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Welcome back!",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification",
                        tint = Color.Yellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1
            Text(
                text = "Rekomendasi untuk kamu",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(2) {
                    TryoutCard(
                        title = "Paket Tryout TIF-3309",
                        soal = "80 Soal",
                        waktu = "90 Menit",
                        color = Color(0xFFFF1744)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2
            Text(
                text = "Ayo mulai tryout kamu!",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(2) {
                    TryoutCard(
                        title = "Paket Tryout TIF-3309",
                        soal = "80 Soal",
                        waktu = "90 Menit",
                        color = Color(0xFFFF9800)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Report
            Text(
                text = "Tryout Reported",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ReportCard()
        }
    }
}

@Composable
fun TryoutCard(title: String, soal: String, waktu: String, color: Color) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(16.dp)
    ) {
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = soal, color = Color.White)
            Text(text = waktu, color = Color.White)
        }
    }
}

@Composable
fun ReportCard() {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tes Penalaran Umum",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simple bar chart (just static boxes)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val data = listOf(30, 60, 40, 20, 55, 70, 50)
                data.forEach {
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(it.dp)
                            .background(Color(0xFFE91E63))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total soal: 80 | Soal benar: 65 | Salah: 15",
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* TODO: Ganti Subjek */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Ganti Subjek", color = Color.White)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TubesPMTheme {
        HomeScreen()
    }
}
