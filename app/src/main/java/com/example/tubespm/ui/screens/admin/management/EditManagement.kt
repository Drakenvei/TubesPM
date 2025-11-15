package com.example.tubespm.ui.screens.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// -------------------------------
// DATA MODEL SECTION
// -------------------------------
data class TryoutSection(
    val id: String,
    val title: String,
    val type: String,       // "TPS" / "Literasi"
    val timeMinutes: Int,
    val questionCount: Int
)

// -------------------------------
// DIALOG – POPUP EDIT MANAGEMENT
// -------------------------------
@Composable
fun EditManagementDialog(
    paket: TryoutPackage,
    onDismiss: () -> Unit,
    onDeactivatePackage: () -> Unit,
    onAddMoreSection: () -> Unit // masih dipertahankan untuk nanti (Firebase, dsb.)
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            EditManagementContent(
                paket = paket,
                onClose = onDismiss,
                onDeactivatePackage = onDeactivatePackage,
                onAddMoreSection = onAddMoreSection
            )
        }
    }
}

// -------------------------------
// KONTEN UTAMA POPUP
// -------------------------------
@Composable
private fun EditManagementContent(
    paket: TryoutPackage,
    onClose: () -> Unit,
    onDeactivatePackage: () -> Unit,
    onAddMoreSection: () -> Unit
) {
    // STATE popup edit section
    var showEditSectionDialog by remember { mutableStateOf(false) }
    var selectedSectionForEdit by remember { mutableStateOf<TryoutSection?>(null) }

    // STATE popup add section
    var showAddSectionDialog by remember { mutableStateOf(false) }

    // DUMMY section
    val sections = remember {
        listOf(
            TryoutSection("1", "Penalaran Umum", "TPS", 20, 20),
            TryoutSection("2", "Pengetahuan Kuantitatif", "TPS", 20, 20),
            TryoutSection("3", "Literasi Indonesia", "Literasi", 20, 20),
            TryoutSection("4", "Literasi B. Inggris", "Literasi", 20, 20)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // Header: back + title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Edit ${paket.name}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF333333)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List section
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sections) { section ->
                SectionCard(
                    section = section,
                    onEditClick = { clicked ->
                        selectedSectionForEdit = clicked
                        showEditSectionDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tombol Add More Section
        OutlinedButton(
            onClick = {
                showAddSectionDialog = true
                onAddMoreSection() // callback eksternal (opsional)
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Gray
            )
        ) {
            Text("Add More Section")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Nonaktifkan Paket
        Button(
            onClick = onDeactivatePackage,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Nonaktifkan Paket",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }

    // ---------- POPUP EDIT SECTION ----------
    if (showEditSectionDialog && selectedSectionForEdit != null) {
        val section = selectedSectionForEdit!!
        EditSectionDialog(
            paketName = paket.name,
            sectionName = section.title,
            initialState = EditSectionUiState(
                type = section.type,
                subtest = section.title,
                timeMinutes = section.timeMinutes,
                questionCount = section.questionCount
            ),
            onDismiss = {
                showEditSectionDialog = false
                selectedSectionForEdit = null
            },
            onSaveSection = {
                // TODO: update ke Firebase / ViewModel
                showEditSectionDialog = false
                selectedSectionForEdit = null
            },
            onEditSoalTryout = {
                // TODO: navigate ke layar edit soal
            }
        )
    }

    // ---------- POPUP ADD SECTION ----------
    if (showAddSectionDialog) {
        AddSectionDialog(
            paketName = paket.name,
            onDismiss = { showAddSectionDialog = false },
            onSaveSection = { newState ->
                // TODO: simpan section baru ke Firebase / ViewModel
                showAddSectionDialog = false
            },
            onEditSoalTryout = {
                // TODO: navigate ke layar edit soal baru
            }
        )
    }
}

// -------------------------------
// CARD SATU SECTION
// -------------------------------
@Composable
private fun SectionCard(
    section: TryoutSection,
    onEditClick: (TryoutSection) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFE0E0E0),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )

                IconButton(
                    onClick = { onEditClick(section) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Edit Section",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            InfoRow(label = "Tipe", value = section.type)
            InfoRow(label = "Waktu", value = "${section.timeMinutes} menit")
            InfoRow(label = "Jumlah Soal", value = "${section.questionCount} soal")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF616161),
            modifier = Modifier.width(90.dp)
        )
        Surface(
            color = Color(0xFF9E9E9E),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditManagementContentPreview() {
    val dummyPackage = TryoutPackage(
        id = "TO-001",
        name = "Tryout Sakti (TO-001)",
        isActive = true,
        tpsSoal = 80,
        tpsMenit = 80,
        literasiSoal = 80,
        literasiMenit = 120
    )
    EditManagementContent(
        paket = dummyPackage,
        onClose = {},
        onDeactivatePackage = {},
        onAddMoreSection = {}
    )
}