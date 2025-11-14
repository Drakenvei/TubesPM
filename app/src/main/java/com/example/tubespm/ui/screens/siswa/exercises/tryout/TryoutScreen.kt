package com.example.tubespm.ui.screens.tryout

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
import androidx.compose.material.icons.filled.Timer
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
import com.example.tubespm.data.model.Section
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.ui.screens.siswa.exercises.tryout.TryoutCatalogItem
import com.example.tubespm.ui.screens.siswa.exercises.tryout.TryoutViewModel
import com.google.firebase.firestore.Query

//import com.example.tubespm.data.model.* // bisa juga import semua

// Main Screen
@Composable
fun TryoutScreen(
    viewModel: TryoutViewModel = hiltViewModel() // 1. Dapatkan ViewModel
) {
    // 2. Observasi state dari ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Ambil state query pencarian dari ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()

    // State lokal untuk dialog bisa tetap di sini
    var selectedTryoutItem by remember { mutableStateOf<TryoutCatalogItem?>(null) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        //Berikan state dan fungsi ke SearchBar
        SearchBarTryout(
            query = searchQuery,
            onQueryChanged = viewModel::onSearchQueryChanged // Cara singkat untuk: { viewModel.onSearchQueryChanged(it) }
        )

        Spacer(Modifier.height(16.dp))

        // Daftar Tryout
        // 3. Tampilkan UI berdasarkan state
        when {
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
            uiState.tryouts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Belum ada tryout yang tersedia.")
                }
            }
            else -> {
                LazyColumn (
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 4. Gunakan data dari uiState.tryouts
                    items(uiState.tryouts) { item ->
                        TryoutCard(item.tryout) { //Kirim 'item.tryout' ke Card
                            selectedTryoutItem = item //Simpan seluruh 'item'
                        }
                    }
                }
            }
        }

    }

    // Modal Detail
    selectedTryoutItem?.let { item -> //'item' adalah TryoutCatalogItem
        TryoutDetailDialog(
            tryout = item.tryout, //Kirim data tryout-nya
            isTaken = item.isTaken, //Kirim status 'isTaken'
            onDismiss = { selectedTryoutItem = null },
            onStart = {
                // Aksi ketika tombol "Ambil Tryout" ditekan
                viewModel.takeTryout(item.tryout)

                // Tutup dialog setelah diambil
                // Misalnya navigasi ke halaman pengerjaan
                selectedTryoutItem = null
            }
        )
    }
}

@Composable
fun SearchBarTryout(
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

// Tryout Card
@Composable
fun TryoutCard(tryout: Tryout, onClick: () -> Unit) {
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
                text = tryout.title,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            Column (verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tryout.sections.forEach { section ->
                    TryoutSectionRow(section = section)
                }
            }
        }
    }
}

@Composable
fun TryoutSectionRow(section: Section) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tag (e.g., "TPS", "Literasi")
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = section.sectionId.uppercase(),
                color = Color(0xFFE61C5D),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.weight(1f))

        //Details
        Icon(
            imageVector = Icons.Filled.Description,
            contentDescription = "Jumlah Soal",
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "${section.sectionQuestionCount} soal",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Spacer(Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Filled.Timer,
            contentDescription = "Durasi",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "${section.sectionDuration} menit",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Detail Dialog
@Composable
fun TryoutDetailDialog(
    tryout: Tryout,
    isTaken: Boolean,
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
                // Header dengan X dan judul
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                                .padding(4.dp)
                        )
                    }

                    Text(
                        text = tryout.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    // Spacer agar teks tetap di tengah meski tombol hanya di kiri
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(Modifier.height(16.dp))

                // Detail Tryout
                Text("Detail Tryout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("Kode Paket: ${tryout.code}", style = MaterialTheme.typography.bodyMedium)
                Text("Jumlah Soal: ${tryout.totalQuestionCount}", style = MaterialTheme.typography.bodyMedium)
                Text("Durasi: ${tryout.totalDuration} menit", style = MaterialTheme.typography.bodyMedium)

                Divider(Modifier.padding(vertical = 12.dp))

                // Sections
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(tryout.sections) { section ->
                        Text(section.sectionName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            section.subtests.forEachIndexed { index, subtest ->
                                Column (modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(
                                        "${index + 1}. ${subtest.subtestName} (${subtest.questionCount} soal, ${subtest.duration} menit)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (subtest.topics.isNotEmpty()) {
                                        Column (modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                                            subtest.topics.forEach { topic ->
                                                Text(
                                                    text = "- ${topic.name}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }

                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onStart,
                    enabled = !isTaken, //Nonaktifkan jika 'isTaken' true
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTaken) Color.Gray else Color(0xFF30D158),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (isTaken) "Sudah Diambil" else "Ambil Tryout",
                        color = if (isTaken) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}