package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.FabPosition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSoalScreen(
    parentId: String,
    type: String,
    sectionName: String? = null,
    subtestId: String? = null,
    sectionId: String? = null,
    paketName: String,
    targetQuestionCount: Int, // Parameter ini sekarang sudah terisi angka (misal: 20)
    currentQuestionCount: Int = 0, // Ini tidak dipakai dari parameter, kita ambil dari viewModel.uiState.questions.size
    onBackClick: () -> Unit,
    onEditQuestion: (String, String, String, String, Int, Int, Boolean) -> Unit,
    onAddQuestion: (String, String, String, Int, String?, Int, Int) -> Unit,
    viewModel: ListSoalViewModel = viewModel()
) {
    // Extract section name dari paketName jika format "Paket - Section"
    val actualSectionName = remember(paketName) {
        if (paketName.contains(" - ")) {
            paketName.split(" - ").getOrNull(1) ?: sectionName
        } else {
            sectionName
        }
    }
    
    // Extract actual paket name (tanpa section)
    val actualPaketName = remember(paketName) {
        if (paketName.contains(" - ")) {
            paketName.split(" - ").first()
        } else {
            paketName
        }
    }
    
    // Load questions saat screen pertama kali dibuka
    LaunchedEffect(parentId, type, subtestId, sectionId, actualSectionName) {
        when {

            // NEW: Logic for Latihan Soal
            type == "latihan_soal" -> {
                viewModel.loadLatihanSoalQuestions(parentId)
            }

            // [REVISI] PRIORITAS UTAMA: Jika ada Subtest ID, pakai itu!
            subtestId != null && subtestId.isNotBlank() -> {
                viewModel.loadQuestionsBySubtest(parentId, type, subtestId)
            }

            // PRIORITAS KEDUA: Baru cek Section
            sectionId != null || actualSectionName != null -> {
                val identifier = sectionId ?: actualSectionName ?: ""
                if (identifier.isNotEmpty()) {
                    viewModel.loadQuestionsBySection(parentId, type, identifier)
                } else {
                    viewModel.loadQuestions(parentId, type)
                }
            }

            // TERAKHIR: Load Semua
            else -> {
                viewModel.loadQuestions(parentId, type)
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Hitung realisasi jumlah soal saat ini
    val realQuestionCount = uiState.questions.size

    val isEditable = uiState.isEditable

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (actualSectionName != null) "Soal - $actualSectionName" else "Daftar Soal",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = actualPaketName,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9966),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            // Tampilkan tombol HANYA JIKA:
            // 1. Target Count = 0 (Unlimited/Latihan Soal mode lama), ATAU
            // 2. Jumlah soal saat ini MASIH KURANG dari Target
            if (isEditable && (targetQuestionCount == 0 || realQuestionCount < targetQuestionCount)) {
                FloatingActionButton(
                    onClick = {
                        // Cari angka questionNumber terbesar yang ada di list saat ini
                        val maxSortNumber = uiState.questions.maxOfOrNull { it.questionNumber } ?: 0

                        // Soal baru pasti lebih besar dari yang paling besar
                        val nextSortNumber = maxSortNumber + 1

                        val nextVisualNumber = uiState.questions.size + 1

                        // LOGIKA UTAMA PERBAIKAN:
                        // Prioritaskan 'subtestId' dari parameter Navigasi.
                        // Jika null, baru coba ambil dari ViewModel state.
                        val targetSubtestId = subtestId ?: uiState.currentSubtestId

                        // Validasi Kritis sebelum pindah layar
                        if (type == "tryout" && targetSubtestId.isNullOrBlank()) {
                            // Tampilkan pesan error jika ID benar-benar hilang (Bug prevention)
                            android.widget.Toast.makeText(
                                context, // Error context di sini, butuh context
                                "Gagal: Subtest ID tidak ditemukan",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onAddQuestion(type, parentId, paketName, nextSortNumber, targetSubtestId, nextVisualNumber, targetQuestionCount)
                        }
                    },
                    modifier = Modifier.offset(x = 0.dp, y = (-80).dp),
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Soal")
                }
            }

        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                // HAPUS .padding(paddingValuesFromNavHost)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            Column (modifier = Modifier.fillMaxSize()) {
                // [BARU] Banner Info Read Only
                if (!isEditable) {
                    Surface(color = Color(0xFFE3F2FD), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mode Lihat (Paket Aktif). Nonaktifkan untuk mengedit.", color = Color(0xFF0D47A1), fontSize = 12.sp)
                        }
                    }
                }

                if (targetQuestionCount > 0) {
                    val isComplete = realQuestionCount >= targetQuestionCount
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isComplete) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isComplete) "Target Terpenuhi" else "Belum Mencapai Target",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Terisi: $realQuestionCount dari $targetQuestionCount soal",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF9966))
                    }
                } else if (uiState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "Terjadi kesalahan",
                                color = Color(0xFFE53935),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                if (subtestId != null) {
                                    viewModel.loadQuestionsBySubtest(parentId, type, subtestId)
                                } else {
                                    viewModel.loadQuestions(parentId, type)
                                }
                            }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                } else if (uiState.questions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Belum ada soal",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tekan tombol + untuk menambah soal",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = "${subtestId}, ${sectionId}, ${sectionName}"
//                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(uiState.questions) { index, question ->
                            // Hitung nomor urut tampilan (selalu rapi: 1, 2, 3...)
                            val displayQuestionNumber = index + 1

                            QuestionCard(
                                question = question,
                                displayNumber = displayQuestionNumber, // <-- Kirim nomor visual ini
                                isEditable = isEditable,
                                onClick = {
                                    // Panggil Navigasi dengan format URL baru
                                    // Kita kirim questionNumber ASLI (untuk DB) dan displayQuestionNumber (untuk Judul)
                                    val route = "admin_edit_question/$type/$parentId/${question.id}/$paketName/${question.questionNumber}?displayNumber=$displayQuestionNumber"
                                    onEditQuestion(type, parentId, question.id, paketName, question.questionNumber, displayQuestionNumber, !isEditable)
                                },
                                onDeleteClick = {
                                    if (isEditable) {
                                        // Tentukan Subtest ID mana yang sedang aktif
                                        // Prioritas: Parameter Navigasi -> State -> Null
                                        val activeSubtestId = subtestId ?: uiState.currentSubtestId

                                        // Panggil fungsi delete dengan parameter baru
                                        viewModel.deleteQuestionSingle(
                                            parentId = parentId,
                                            type = type,
                                            questionId = question.id,
                                            currentSubtestId = activeSubtestId // <--- KIRIM ID INI
                                        )
                                    }

                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: com.example.tubespm.data.model.QuizQuestion,
    displayNumber: Int, // Terima parameter baru ini
    isEditable: Boolean, // Parameter baru
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Soal $displayNumber", // (Client side numbering)
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.questionText.take(100) + if (question.questionText.length > 100) "..." else "",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    maxLines = 2
                )
            }
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditable) {
                    // Mode Edit: Tampilkan Hapus & Edit Icon
                    IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFE53935)) }
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF757575), modifier = Modifier.size(24.dp))
                } else {
                    // Mode View: Tampilkan Mata (Lihat) atau Gembok
                    Icon(Icons.Default.Visibility, contentDescription = "Lihat Detail", tint = Color.LightGray, modifier = Modifier.size(24.dp))
                }
            }

        }
    }
}

