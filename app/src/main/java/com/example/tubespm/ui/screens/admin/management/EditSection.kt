package com.example.tubespm.ui.screens.admin.management

import android.widget.Toast
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
// DIALOG EDIT SECTION
// ======================================
@Composable
fun EditSectionDialog(
    tryoutId: String,       // <-- Butuh ID Tryout
    sectionId: String,      // <-- Butuh ID Section yang diedit
    paketName: String,
    sectionName: String,
    initialState: EditSectionUiState,
    onDismiss: () -> Unit,
    onEditSoalTryout: () -> Unit,
    // Inject ViewModel
    viewModel: EditSectionViewModel = viewModel()
) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    SectionFormDialog(
        title = "Edit Section",
        primaryButtonText = if (isSaving) "Saving..." else "Edit Section",
        paketName = paketName,
        initialState = initialState,
        onDismiss = onDismiss,
        onConfirm = { data ->
            if (isSaving) return@SectionFormDialog
            isSaving = true
            viewModel.updateSection(
                tryoutId = tryoutId,
                oldSectionId = sectionId,
                sectionData = data,
                onSuccess = {
                    Toast.makeText(context, "Section Updated", Toast.LENGTH_SHORT).show()
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
    onEditSoalTryout: () -> Unit,
    // Inject ViewModel
    viewModel: EditSectionViewModel = viewModel()
) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    SectionFormDialog(
        title = "Add Section",
        primaryButtonText = if (isSaving) "Saving..." else "Tambah Section",
        paketName = paketName,
        initialState = initialState,
        onDismiss = onDismiss,
        onConfirm = { data ->
            if (isSaving) return@SectionFormDialog
            isSaving = true
            viewModel.addSection(
                tryoutId = tryoutId,
                sectionData = data,
                onSuccess = {
                    Toast.makeText(context, "Section Added", Toast.LENGTH_SHORT).show()
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
    onEditSoalTryout: () -> Unit
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
                modifier = Modifier.background(Color(0xFFFDFDFD)).padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // ... (Header Code sama seperti sebelumnya) ...
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121))
                        Text(text = paketName, fontSize = 13.sp, color = Color(0xFF616161))
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(modifier = Modifier.width(140.dp), color = Color(0xFFBDBDBD))
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Tipe Tryout Dropdown
                Text("Tipe Tryout", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                // ... (Implementasi Dropdown Code sama) ...
                // Agar singkat, saya asumsikan dropdown code sama persis

                Spacer(modifier = Modifier.height(12.dp))

                // Jenis Subtest Dropdown
                Text("Jenis Subtest", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                // ... (Implementasi Dropdown Subtest Code sama) ...

                // Waktu & Soal Inputs
                // ... (Implementasi Input Number Code sama) ...

                // BUTTON UTAMA
                Button(
                    onClick = { onConfirm(uiState) },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(primaryButtonText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}