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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.sampleLatihanList
import com.example.tubespm.ui.screens.siswa.exercises.latihansoal.LatihanSoalViewModel

// Main Screen
@Composable
fun LatihanSoalScreen(
    viewModel: LatihanSoalViewModel = hiltViewModel() // 1. Dapatkan ViewModel
){
    val uiState by viewModel.uiState.collectAsState() // 2. Observasi state

    // ambil state query pencarian dari ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedLatihan by remember { mutableStateOf<LatihanSoal?>(null) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Berikan state dan fungsi ke SearchBar
        SearchBarLatihanSoal(
            query = searchQuery,
            onQueryChanged = viewModel::onSearchQueryChanged

        )

        Spacer(Modifier.height(16.dp))

        // 3. Tampilkan UI berdasarkan state
        when{
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.latihanSoal.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Belum ada latihan yang tersedia.")
                }
            }
            else -> {
                LazyColumn (
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 4. Gunakan data dari uiState.exercises
                    items(uiState.latihanSoal) { latihan ->
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
    query: String,
    onQueryChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
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