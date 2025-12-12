package com.example.tubespm.ui.screens.admin.management

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel


// ======================================
// DIALOG EDIT SECTION (Sebenarnya Edit Subtest)
// ======================================
@Composable
fun EditSectionDialog(
    tryoutId: String,       // <-- Butuh ID Tryout
    sectionId: String,      // <-- Butuh ID Section yang diedit
    paketName: String,
    sectionName: String,
    initialState: EditSectionUiState,
    onDismiss: () -> Unit,
    onEditSoalTryout: (String, String, String, String, Int) -> Unit, // (type, parentId, questionId, paketName, questionNumber)
    // Inject ViewModel
    viewModel: EditSectionViewModel = viewModel()
) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    SectionFormDialog(
        title = "Edit Subtest",
        primaryButtonText = if (isSaving) "Saving..." else "Edit Subtest",
        paketName = paketName,
        initialState = initialState,
        onDismiss = onDismiss,
        onConfirm = { data ->
            if (isSaving) return@SectionFormDialog
            isSaving = true
            // PANGGIL FUNGSI SAVE BARU
            viewModel.saveSubtest(
                tryoutId = tryoutId,
                subtestIdToEdit = sectionId, // Kirim ID untuk diedit
                data = data,
                onSuccess = {
                    Toast.makeText(context, "Subtest Updated", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                onError = { msg ->
                    isSaving = false
                    Toast.makeText(context, "Error: $msg", Toast.LENGTH_SHORT).show()
                }
            )
        },
        onEditSoalTryout = onEditSoalTryout
    )
}

// ======================================
// DIALOG ADD SECTION
// ======================================
@Composable
fun AddSectionDialog(
    tryoutId: String, // <-- Butuh ID Tryout
    paketName: String,
    initialState: EditSectionUiState = EditSectionUiState(type = "TPS", subtest = "Penalaran Umum", timeMinutes = 0, questionCount = 0),
    onDismiss: () -> Unit,
    onEditSoalTryout: (String, String, String, String, Int) -> Unit, // (type, parentId, questionId, paketName, questionNumber)
    // Inject ViewModel
    viewModel: EditSectionViewModel = viewModel()
) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    SectionFormDialog(
        title = "Tambah Subtest",
        primaryButtonText = if (isSaving) "Saving..." else "Tambah Subtest",
        paketName = paketName,
        initialState = initialState,
        onDismiss = onDismiss,
        onConfirm = { data ->
            if (isSaving) return@SectionFormDialog
            isSaving = true
            viewModel.saveSubtest(
                tryoutId = tryoutId,
                subtestIdToEdit = null, // Null artinya Add Baru
                data = data,
                onSuccess = {
                    Toast.makeText(context, "Subtest Added", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                onError = { msg ->
                    isSaving = false
                    Toast.makeText(context, "Error: $msg", Toast.LENGTH_SHORT).show()
                }
            )
        },
        onEditSoalTryout = onEditSoalTryout
    )
}

// ======================================
// FORM DIALOG REUSABLE (UI Murni)
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionFormDialog(
    title: String,
    primaryButtonText: String,
    paketName: String,
    initialState: EditSectionUiState,
    onDismiss: () -> Unit,
    onConfirm: (EditSectionUiState) -> Unit,
    onEditSoalTryout: (String, String, String, String, Int) -> Unit // (type, parentId, questionId, paketName, questionNumber)
) {
    var uiState by remember { mutableStateOf(initialState) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedSubtest by remember { mutableStateOf(false) }

    val typeOptions = listOf("TPS", "Literasi")

    val tpsOptions = listOf(
        "Penalaran Umum",
        "Pengetahuan Kuantitatif",
        "Pengetahuan dan Pemahaman Umum",
        "Pemahaman Bacaan dan Menulis"
    )

    val literasiOptions = listOf(
        "Literasi dalam Bahasa Indonesia",
        "Literasi dalam Bahasa Inggris",
        "Penalaran Matematika"
    )

    // Pilih opsi berdasarkan Tipe yang sedang dipilih
    val currentSubtestOptions = if (uiState.type == "TPS") tpsOptions else literasiOptions

    // Reset subtest jika tipe berubah (agar tidak ada subtest Literasi di tipe TPS)
    LaunchedEffect(uiState.type) {
        // Jika tidak valid, reset ke opsi pertama dari list yang baru
        uiState = uiState.copy(subtest = currentSubtestOptions.firstOrNull() ?: "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(Color(0xFFFDFDFD))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF212121)
                        )
                        Text(text = paketName, fontSize = 13.sp, color = Color(0xFF616161))
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(modifier = Modifier.width(140.dp), color = Color(0xFFBDBDBD))
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Tipe Tryout Dropdown
                Text(
                    "Tipe Tryout",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = uiState.type,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121),
                            cursorColor = Color(0xFF212121)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    uiState = uiState.copy(type = option)
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Jenis Subtest Dropdown
                Text(
                    "Jenis Subtest",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedSubtest,
                    onExpandedChange = { expandedSubtest = !expandedSubtest }
                ) {
                    OutlinedTextField(
                        value = uiState.subtest,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubtest) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121),
                            cursorColor = Color(0xFF212121)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSubtest,
                        onDismissRequest = { expandedSubtest = false }
                    ) {
                        currentSubtestOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    uiState = uiState.copy(subtest = option)
                                    expandedSubtest = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Waktu Input
                Text(
                    "Waktu (Menit)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.timeMinutes.toString(),
                    onValueChange = {
                        val value = it.toIntOrNull() ?: 0
                        uiState = uiState.copy(timeMinutes = value)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121),
                        cursorColor = Color(0xFF212121)
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Jumlah Soal Input
                Text(
                    "Jumlah Soal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.questionCount.toString(),
                    onValueChange = {
                        val value = it.toIntOrNull() ?: 0
                        uiState = uiState.copy(questionCount = value)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121),
                        cursorColor = Color(0xFF212121)
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Kisi-kisi (Pisahkan dengan koma)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.topicsString,
                    onValueChange = { newValue ->
                        uiState = uiState.copy(topicsString = newValue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {Text("Contoh: Aljabar, Geometri, Logika", color = Color(0xFF9E9E9E))},
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121),
                        cursorColor = Color(0xFF212121)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(24.dp))

                // BUTTON UTAMA
                Button(
                    onClick = { onConfirm(uiState) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(
                        text = primaryButtonText,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}