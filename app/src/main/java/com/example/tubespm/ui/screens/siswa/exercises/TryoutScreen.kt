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
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.TryoutSection
import com.example.tubespm.repository.TryoutRepository
import kotlinx.coroutines.launch

@Composable
fun TryoutScreen() {
    val repository = remember { TryoutRepository() }
    val scope = rememberCoroutineScope()

    var tryouts by remember { mutableStateOf<List<Tryout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTryout by remember { mutableStateOf<Tryout?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Load tryouts on first composition
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                tryouts = repository.getAllTryouts()
            } catch (e: Exception) {
                errorMessage = "Gagal memuat data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Handle search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            scope.launch {
                try {
                    tryouts = repository.searchTryouts(searchQuery)
                } catch (e: Exception) {
                    errorMessage = "Gagal mencari: ${e.message}"
                }
            }
        } else {
            scope.launch {
                tryouts = repository.getAllTryouts()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        SearchBarTryout(
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
                                    tryouts = repository.getAllTryouts()
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
            tryouts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank())
                            "Belum ada tryout tersedia"
                        else
                            "Tidak ada hasil untuk \"$searchQuery\"",
                        color = Color.Gray
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tryouts) { tryout ->
                        TryoutCard(tryout) {
                            selectedTryout = tryout
                        }
                    }
                }
            }
        }
    }

    // Modal Detail
    selectedTryout?.let {
        TryoutDetailDialog(
            tryout = it,
            onDismiss = { selectedTryout = null },
            onStart = {
                selectedTryout = null
                // Navigate to quiz screen
            }
        )
    }
}

@Composable
fun SearchBarTryout(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search...", color = Color.Black.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black) },
        trailingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null, tint = Color.Black) },
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

@Composable
fun TryoutCard(tryout: Tryout, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE61C5D))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = tryout.title,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tryout.sections.forEach { section ->
                    TryoutSectionRow(section = section)
                }
            }
        }
    }
}

@Composable
fun TryoutSectionRow(section: TryoutSection) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = section.title,
                color = Color(0xFFE61C5D),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.weight(1f))

        Icon(
            imageVector = Icons.Filled.Description,
            contentDescription = "Jumlah Soal",
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "${section.totalQuestions} soal",
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
            text = "${section.totalDuration} menit",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TryoutDetailDialog(
    tryout: Tryout,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
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

                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(Modifier.height(16.dp))

                Text("Detail Tryout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("Kode Paket: ${tryout.code}", style = MaterialTheme.typography.bodyMedium)
                Text("Jumlah Soal: ${tryout.totalQuestions}", style = MaterialTheme.typography.bodyMedium)
                Text("Durasi: ${tryout.totalDuration} menit", style = MaterialTheme.typography.bodyMedium)

                Divider(Modifier.padding(vertical = 12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(tryout.sections) { section ->
                        Text(section.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            section.subSections.forEachIndexed { index, subSection ->
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(
                                        "${index + 1}. ${subSection.name} (${subSection.questionCount} soal, ${subSection.duration} menit)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (subSection.kisiKisi.isNotEmpty()) {
                                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                                            subSection.kisiKisi.forEach { kisi ->
                                                Text(
                                                    text = "- $kisi",
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "Ambil Tryout",
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