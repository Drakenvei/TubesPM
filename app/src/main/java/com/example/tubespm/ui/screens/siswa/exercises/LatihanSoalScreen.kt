package com.example.tubespm.ui.screens.latihansoal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.repository.LatihanRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Main Screen
@Composable
fun LatihanSoalScreen(){
    val repository = remember { LatihanRepository() }
    val scope = rememberCoroutineScope()

    var latihanList by remember { mutableStateOf<List<LatihanSoal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedLatihan by remember { mutableStateOf<LatihanSoal?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(Unit) {
        android.util.Log.d("LatihanScreen", "🔐 Current user: ${currentUser?.email ?: "NOT LOGGED IN"}")
        android.util.Log.d("LatihanScreen", "🔐 User UID: ${currentUser?.uid ?: "null"}")
    }

    // Load latihan on first composition
    LaunchedEffect(Unit) {
        scope.launch {
            android.util.Log.d("LatihanScreen", "=== START LOADING ===")
            isLoading = true
            errorMessage = null
            try {
                android.util.Log.d("LatihanScreen", "Calling repository.getAllLatihan()...")
                val result = repository.getAllLatihan()
                android.util.Log.d("LatihanScreen", "✅ Got ${result.size} items from Firebase")
                result.forEachIndexed { index, item ->
                    android.util.Log.d("LatihanScreen", "  [$index] ${item.title}")
                }
                latihanList = result
                android.util.Log.d("LatihanScreen", "State updated with ${latihanList.size} items")
            } catch (e: Exception) {
                android.util.Log.e("LatihanScreen", "❌ ERROR: ${e.message}", e)
                errorMessage = "Gagal memuat data: ${e.message}"
            } finally {
                isLoading = false
                android.util.Log.d("LatihanScreen", "=== END LOADING (isLoading=$isLoading) ===")
            }
        }
    }

    // Handle search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            scope.launch {
                try {
                    latihanList = repository.searchLatihan(searchQuery)
                } catch (e: Exception) {
                    errorMessage = "Gagal mencari: ${e.message}"
                }
            }
        } else {
            scope.launch {
                latihanList = repository.getAllLatihan()
            }
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        SearchBarLatihanSoal(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFE61C5D))
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Terjadi kesalahan",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    latihanList = repository.getAllLatihan()
                                    isLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE61C5D)
                            )
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            latihanList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank())
                            "Belum ada latihan soal tersedia"
                        else
                            "Tidak ada hasil untuk \"$searchQuery\"",
                        color = Color.Gray
                    )
                }
            }
            else -> {
                LazyColumn (
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(latihanList) { latihan ->
                        LatihanSoalCard(latihan) {
                            selectedLatihan = latihan
                        }
                    }
                }
            }
        }
    }

    // Modal Detail
    selectedLatihan?.let {
        LatihanDetailDialog(
            latihan = it,
            onDismiss = { selectedLatihan = null },
            onStart = {
                // Aksi ketika tombol "Ambil Latihan" ditekan
                selectedLatihan = null
            }
        )
    }
}

// Search Bar
@Composable
fun SearchBarLatihanSoal(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search...", color = Color.Black.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black) },
        trailingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null, tint = Color.Black)},
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = Color.Gray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
        singleLine = true
    )
}

// Latihan Card
@Composable
fun LatihanSoalCard(latihan: LatihanSoal, onClick: () -> Unit) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .clickable {onClick()},
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column (
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = latihan.title,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            //subtest tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = latihan.subtest,
                    color = Color(0xFFE61C5D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(12.dp))

            // Question Count
            Row (verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = "Jumlah Soal",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${latihan.questionCount} soal",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Detail Dialog
@Composable
fun LatihanDetailDialog(
    latihan: LatihanSoal,
    onDismiss: () -> Unit,
    onStart: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header dengan tombol X dan judul di tengah
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                                .padding(4.dp)
                        )
                    }

                    Text(
                        text = latihan.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    // Spacer agar teks tetap di tengah secara visual
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(Modifier.height(16.dp))

                // Detail Latihan
                Text("Detail Latihan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("Subtest: ${latihan.subtest}", style = MaterialTheme.typography.bodyMedium)
                Text("Jumlah Soal: ${latihan.questionCount} soal", style = MaterialTheme.typography.bodyMedium)

                Divider(Modifier.padding(vertical = 12.dp))

                // Kisi-kisi
                Text("Kisi-kisi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    latihan.kisiKisi.forEach { item ->
                        Text(
                            "- $item",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Tombol Ambil Latihan
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "Ambil Latihan",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}