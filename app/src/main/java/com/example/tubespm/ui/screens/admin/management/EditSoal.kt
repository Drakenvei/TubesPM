package com.example.tubespm.ui.screens.admin.management

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage

data class AnswerOption(
    val label: Char,
    val placeholder: String = "Add Here"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionScreen(
    tryoutId: String,           // <-- Perlu ID Tryout/Latihan untuk database
    questionId: String,         // <-- Perlu ID Soal untuk database
    paketName: String,
    questionNumber: Int = 1,      // Ini ID Database (misal 5)
    displayNumber: Int = 1,       // [BARU] Ini ID Visual (misal 4)
    paddingValuesFromNavHost: PaddingValues,
    onBackClick: () -> Unit,
    type: String = "tryout",    // <-- "tryout" atau "latihan_soal"
    isReadOnly: Boolean = false, // [BARU] Parameter
    // Inject ViewModel
    viewModel: EditQuestionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                // Pastikan ExerciseCatalogRepository tersedia
                return EditQuestionViewModel() as T
            }
        }
    )
) {
    // Load data saat pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.loadQuestion(tryoutId, questionId, type)
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle efek setelah simpan sukses
    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            Toast.makeText(context, "Perubahan disimpan", Toast.LENGTH_SHORT).show()
            onBackClick() // Kembali ke layar sebelumnya jika sukses simpan
        }
    }

    val options = remember {
        listOf(AnswerOption('A'), AnswerOption('B'), AnswerOption('C'), AnswerOption('D'), AnswerOption('E'))
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        // Active State
        focusedContainerColor = Color(0xFFE0E0E0),
        unfocusedContainerColor = Color(0xFFE0E0E0),
        focusedBorderColor = Color(0xFFFF9966),
        unfocusedBorderColor = Color.Transparent,
        focusedTextColor = Color(0xFF212121),
        unfocusedTextColor = Color(0xFF212121),

        // Read Only State (Tetap hitam agar terbaca, background sedikit abu)
        disabledContainerColor = Color(0xFFF5F5F5), // Abu sangat muda
        disabledTextColor = Color(0xFF424242), // Hitam pekat
        disabledBorderColor = Color.Transparent,
        disabledPlaceholderColor = Color.Gray
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = if (isReadOnly) "Detail Soal (Lihat)" else "Edit Soal", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = paketName, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Sembunyikan Tombol Save jika Read Only
                    if (!isReadOnly) {
                        IconButton(
                            onClick = {
                                viewModel.saveQuestion(context, tryoutId, questionId, type, onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() })
                            },
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                        }
                    } else {
                        // Icon Gembok penanda terkunci
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9966),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(paddingValuesFromNavHost)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF9966))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {

                    Text(text = "Soal $displayNumber", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                    Spacer(modifier = Modifier.height(16.dp))

                    // ------------------ TULIS SOAL ------------------
                    Text(text = "Tulis Soal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = uiState.questionData.questionText,
                        onValueChange = { viewModel.updateQuestionText(it) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = isReadOnly,
                        enabled = true,
                        placeholder = { Text("Tulis Soal Di Sini", color = Color(0xFF9E9E9E)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = if (isReadOnly) textFieldColors.copy(
                            focusedContainerColor = Color(0xFFEEEEEE),
                            unfocusedContainerColor = Color(0xFFEEEEEE)
                        ) else textFieldColors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ------------------ TAMBAH GAMBAR ------------------
                    Text(text = "Gambar Soal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))

                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let { viewModel.updateQuestionImageUri(it) }
                    }

                    // Tampilkan Gambar jika ada (URI baru atau Existing URL/Base64)
                    val currentQuestionImage = uiState.questionImageUri ?: uiState.questionData.questionImage

                    if (currentQuestionImage != null && currentQuestionImage.toString().isNotEmpty()) {
                        EditImagePreviewWithDelete(
                            model = currentQuestionImage,
                            onDelete = { viewModel.deleteQuestionImage() },
                            canDelete = !isReadOnly
                        )
                    } else if (!isReadOnly) {
                        // Hanya tampilkan tombol tambah jika BUKAN ReadOnly
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Picture", tint = Color(0xFF9E9E9E), modifier = Modifier.size(32.dp))
                        }
                    } else {
                        Text("- Tidak ada gambar -", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ------------------ PILIHAN JAWABAN ------------------
                    Text(text = "Pilihan Jawaban", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))) {
                        options.forEachIndexed { index, option ->
                            val labelStr = option.label.toString()
                            val isCorrect = uiState.questionData.correctAnswer == labelStr

                            val optionImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                                uri?.let { viewModel.updateOptionImageUri(labelStr, it) }
                            }

                            // Cek apakah ada gambar (Baru atau Lama)
                            val currentOptionUri = uiState.optionImageUris[labelStr]
                            val currentOptionUrl = uiState.questionData.optionImages.getOrNull(index)

                            // Priority: New URI > Existing URL/Base64
                            val imageModel = currentOptionUri ?: currentOptionUrl
                            val hasImage = imageModel != null && imageModel.toString().isNotEmpty()

                            Column (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "(${labelStr})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF757575),
                                        modifier = Modifier.width(32.dp)
                                    )
                                    OutlinedTextField(
                                        value = uiState.answerMap[labelStr] ?: "",
                                        onValueChange = { viewModel.updateAnswerOption(labelStr, it) },
                                        modifier = Modifier.weight(1f),
                                        readOnly = isReadOnly,
                                        enabled = true,
                                        placeholder = { Text(if (hasImage) "Teks (Opsional)" else "Teks Jawaban", fontSize = 13.sp, color = Color.Gray) },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            color = Color(0xFF212121)
                                        ),
                                        colors = if (isReadOnly) textFieldColors.copy(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        ) else textFieldColors.copy(
                                            focusedContainerColor = Color(0xFFF5F5F5),
                                            unfocusedContainerColor = Color(0xFFF5F5F5)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // [PERBAIKAN] Tombol Gambar menggunakan IconButton
                                    IconButton(
                                        onClick = { optionImageLauncher.launch("image/*") },
                                        enabled = !isReadOnly, // Disable jika ReadOnly
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (hasImage) Color(0xFFBBDEFB) else if (isReadOnly) Color(0xFFF5F5F5) else Color.White,
                                                CircleShape
                                            )
                                            .border(1.dp, if(isReadOnly) Color.Transparent else Color(0xFFE0E0E0), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = "Add Image",
                                            tint = if (hasImage) Color(0xFF1976D2) else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // [PERBAIKAN] Tombol Centang Jawaban Benar menggunakan IconButton
                                    IconButton(
                                        onClick = { viewModel.setCorrectAnswer(labelStr) },
                                        enabled = !isReadOnly, // Disable jika ReadOnly
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (isCorrect) Color(0xFF4CAF50) else if (isReadOnly) Color(0xFFF5F5F5) else Color.White,
                                                CircleShape
                                            )
                                            .border(1.dp, if(isCorrect || isReadOnly) Color.Transparent else Color(0xFFE0E0E0), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Correct",
                                            // Warna Icon Tetap Putih jika benar (walau ReadOnly), Abu jika salah
                                            tint = if (isCorrect) Color.White else Color.LightGray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Preview Gambar Opsi
                                if (hasImage) {
                                    Box(modifier = Modifier.padding(start = 40.dp, top = 8.dp, bottom = 8.dp)) {
                                        EditImagePreviewWithDelete(
                                            model = imageModel,
                                            onDelete = { viewModel.deleteOptionImage(labelStr) },
                                            canDelete = !isReadOnly,
                                            size = 100.dp
                                        )
                                    }
                                }
                            }
                            if (option.label != 'E') HorizontalDivider(color = Color(0xFFCCCCCC), thickness = 0.5.dp)                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ------------------ PEMBAHASAN ------------------
                    Text(text = "Pembahasan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                    Spacer(modifier = Modifier.height(4.dp))

                    val explanationImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        uri?.let { viewModel.updateExplanationImageUri(it) }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isReadOnly) Color(0xFFEEEEEE) else Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, if(isReadOnly) Color.Transparent else Color.Gray, RoundedCornerShape(6.dp))
                            .padding(1.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.questionData.discussion,
                            onValueChange = { viewModel.updateDiscussionText(it) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            readOnly = isReadOnly,
                            enabled = true,
                            placeholder = { Text("Tulis Pembahasan Di Sini", color = Color(0xFF9E9E9E)) },
                            colors = textFieldColors.copy(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            )
                        )
                        // Tombol Add Image Pembahasan (Hanya jika editable)
                        if (!isReadOnly) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { explanationImageLauncher.launch("image/*") },
                                    modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Image", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    // Preview Gambar Pembahasan
                    val currentExplanationImage = uiState.explanationImageUri ?: uiState.questionData.explanationImage

                    if (currentExplanationImage != null && currentExplanationImage.toString().isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        EditImagePreviewWithDelete(
                            model = currentExplanationImage,
                            onDelete = { viewModel.deleteExplanationImage() },
                            canDelete = !isReadOnly,
                            size = 150.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                }
            }
        }
    }
}

@Composable
fun EditImagePreviewWithDelete(
    model: Any?, // Can be Uri or String (Url/Base64)
    onDelete: () -> Unit,
    canDelete: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 120.dp
) {
    if (model == null) return

    val finalModel = remember(model) {
        if (model is String) {
            try {
                // 1. Bersihkan prefix jika ada (misal: "data:image/png;base64,")
                val cleanBase64 = if (model.contains(",")) model.split(",")[1] else model

                // 2. Decode String Base64 menjadi ByteArray
                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

                // 3. Ubah ByteArray menjadi Bitmap
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                // Jika gagal decode (mungkin URL biasa), kembalikan aslinya
                model
            }
        } else {
            // Jika model adalah Uri (gambar baru dari galeri), biarkan saja
            model
        }
    }

    Box(
        modifier = Modifier.size(size)
    ) {
        AsyncImage(
            model = finalModel,
            contentDescription = "Preview Image",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp, end = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        if (canDelete) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .background(Color.Red, CircleShape)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Image",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
}