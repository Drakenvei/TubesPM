package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// ======================================
// STATE UNTUK EDIT / ADD SECTION
// ======================================
data class EditSectionUiState(
    val type: String = "TPS",              // "TPS" / "Literasi"
    val subtest: String = "Penalaran Umum",
    val timeMinutes: Int = 20,
    val questionCount: Int = 20
)

// ======================================
// DIALOG EDIT SECTION
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSectionDialog(
    paketName: String,
    sectionName: String,
    initialState: EditSectionUiState,
    onDismiss: () -> Unit,
    onSaveSection: (EditSectionUiState) -> Unit,
    onEditSoalTryout: () -> Unit
) {
    SectionFormDialog(
        title = "Edit Section",
        primaryButtonText = "Edit Section",
        paketName = paketName,
        initialState = initialState,
        onDismiss = onDismiss,
        onConfirm = onSaveSection,
        onEditSoalTryout = onEditSoalTryout
    )
}

// ======================================
// DIALOG ADD SECTION
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSectionDialog(
    paketName: String,
    initialState: EditSectionUiState = EditSectionUiState(
        type = "TPS",
        subtest = "Penalaran Umum",
        timeMinutes = 0,
        questionCount = 0
    ),
    onDismiss: () -> Unit,
    onSaveSection: (EditSectionUiState) -> Unit,
    onEditSoalTryout: () -> Unit
) {
    SectionFormDialog(
        title = "Add Section",
        primaryButtonText = "Tambah Section",
        paketName = paketName,
        initialState = initialState,
        onDismiss = onDismiss,
        onConfirm = onSaveSection,
        onEditSoalTryout = onEditSoalTryout
    )
}

// ======================================
// FORM DIALOG YANG DIPAKAI BERSAMA
// ======================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionFormDialog(
    title: String,
    primaryButtonText: String,            // "Edit Section" / "Tambah Section"
    paketName: String,
    initialState: EditSectionUiState,
    onDismiss: () -> Unit,
    onConfirm: (EditSectionUiState) -> Unit,
    onEditSoalTryout: () -> Unit         // 🔴 tombol khusus untuk ke halaman Edit Soal
) {
    var uiState by remember { mutableStateOf(initialState) }

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
            ) {

                // ---------------- HEADER ----------------
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,                  // Edit / Add Section
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = paketName,
                            fontSize = 13.sp,
                            color = Color(0xFF616161)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(
                            modifier = Modifier
                                .width(140.dp)
                                .align(Alignment.CenterHorizontally),
                            color = Color(0xFFBDBDBD)
                        )
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ====================== TIPE TRYOUT ======================
                Text(
                    text = "Tipe Tryout",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))

                var typeExpanded by remember { mutableStateOf(false) }
                val typeOptions = listOf("TPS", "Literasi")

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.fillMaxWidth(0.55f)
                ) {
                    TextField(
                        value = uiState.type,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = typeExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            disabledContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            color = Color(0xFF212121),
                            fontSize = 14.sp
                        ),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    uiState = uiState.copy(type = option)
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ====================== JENIS SUBTEST ======================
                Text(
                    text = "Jenis Subtest",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))

                var subtestExpanded by remember { mutableStateOf(false) }
                val subtestOptions = listOf(
                    "Penalaran Umum",
                    "Pengetahuan Kuantitatif",
                    "Literasi Indonesia",
                    "Literasi B. Inggris"
                )

                ExposedDropdownMenuBox(
                    expanded = subtestExpanded,
                    onExpandedChange = { subtestExpanded = !subtestExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = uiState.subtest,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = subtestExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            disabledContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            color = Color(0xFF212121),
                            fontSize = 14.sp
                        ),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = subtestExpanded,
                        onDismissRequest = { subtestExpanded = false }
                    ) {
                        subtestOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    uiState = uiState.copy(subtest = option)
                                    subtestExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ====================== WAKTU PENGERJAAN ======================
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Waktu Pengerjaan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.timeMinutes.toString(),
                        onValueChange = { newText ->
                            val clean = newText.filter { it.isDigit() }
                            val newInt = clean.toIntOrNull() ?: 0
                            uiState = uiState.copy(
                                timeMinutes = newInt.coerceIn(0, 300)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.width(80.dp),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            cursorColor = Color(0xFF212121),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Menit",
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ====================== BANYAK SOAL ======================
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Banyak soal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.questionCount.toString(),
                        onValueChange = { newText ->
                            val clean = newText.filter { it.isDigit() }
                            val newInt = clean.toIntOrNull() ?: 0
                            uiState = uiState.copy(
                                questionCount = newInt.coerceIn(0, 500)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.width(80.dp),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            cursorColor = Color(0xFF212121),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Soal",
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ====================== BUTTON EDIT SOAL TRYOUT ======================
                Button(
                    onClick = onEditSoalTryout,        // 👉 pindah ke halaman Edit Soal
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Edit Soal Tryout",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ====================== BUTTON UTAMA (EDIT / TAMBAH SECTION) ======================
                Button(
                    onClick = { onConfirm(uiState) },   // hanya simpan data section
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = primaryButtonText,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddSectionDialogPreview() {
    AddSectionDialog(
        paketName = "Tryout Sakti (TO-001)",
        onDismiss = {},
        onSaveSection = {},
        onEditSoalTryout = {}
    )
}
