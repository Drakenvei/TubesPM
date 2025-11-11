package com.example.tubespm.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubespm.ui.theme.TubesPMTheme

data class TryoutPackage(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val tpsSoal: Int,
    val tpsMenit: Int,
    val literasiSoal: Int,
    val literasiMenit: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenTryoutScreen() {
    var searchQuery by remember { mutableStateOf("") }

    val tryoutPackages = remember {
        listOf(
            TryoutPackage(
                id = "TO-001",
                name = "Paket Tryout Sakti (TO-001)",
                isActive = true,
                tpsSoal = 80,
                tpsMenit = 80,
                literasiSoal = 80,
                literasiMenit = 120
            ),
            TryoutPackage(
                id = "TO-002",
                name = "Paket Tryout Sakti (TO-002)",
                isActive = false,
                tpsSoal = 80,
                tpsMenit = 80,
                literasiSoal = 80,
                literasiMenit = 120
            ),
            TryoutPackage(
                id = "TO-003",
                name = "Paket Tryout Sakti (TO-003)",
                isActive = false,
                tpsSoal = 0,
                tpsMenit = 0,
                literasiSoal = 0,
                literasiMenit = 0
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Clipboard",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Manajemen Tryout",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9966)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle add new tryout */ },
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Tryout",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Gray
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Activity") },
                    label = { Text("Activity") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Tryout List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tryoutPackages) { tryoutPackage ->
                    TryoutPackageCard(tryoutPackage)
                }
            }
        }
    }
}

@Composable
fun TryoutPackageCard(tryoutPackage: TryoutPackage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE91E63)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with title, status and settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tryoutPackage.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (tryoutPackage.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "active",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                IconButton(onClick = { /* Handle settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            if (!tryoutPackage.isActive && tryoutPackage.id == "TO-002") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "inactive",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TPS Section
            TryoutSection(
                title = "TPS",
                soal = tryoutPackage.tpsSoal,
                menit = tryoutPackage.tpsMenit
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Literasi Section
            TryoutSection(
                title = "Literasi",
                soal = tryoutPackage.literasiSoal,
                menit = tryoutPackage.literasiMenit
            )
        }
    }
}

@Composable
fun TryoutSection(title: String, soal: Int, menit: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFFE91E63),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Soal",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (soal > 0) "$soal soal" else "- soal",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Waktu",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (menit > 0) "$menit menit" else "- menit",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManajemenTryoutScreenPreview() {
    TubesPMTheme {
        ManajemenTryoutScreen()
    }
}