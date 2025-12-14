package com.example.tubespm.ui.screens.admin.management

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

// -------------------------------
// DIALOG – POPUP EDIT MANAGEMENT
// -------------------------------
@Composable
fun EditManagementDialog(
    paket: TryoutPackage,
    onDismiss: () -> Unit,
    onDeactivatePackage: () -> Unit, // Callback ke parent (opsional, bisa dihandle VM juga)
    onAddMoreSection: () -> Unit,
    onGoToEditQuestion: (String, String, String, String, Int) -> Unit, // (type, parentId, questionId, paketName, questionNumber)
    // Inject ViewModel
    viewModel: EditManagementViewModel = viewModel()
) {
    // Load data saat Dialog pertama kali dibuka
    LaunchedEffect(paket.id) {
        viewModel.loadSections(paket.id)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            EditManagementContent(
                paket = paket,
                viewModel = viewModel, // Pass VM ke konten
                onClose = onDismiss,
                onDeactivatePackage = onDeactivatePackage,
                onAddMoreSection = onAddMoreSection,
                onGoToEditQuestion = onGoToEditQuestion
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
    viewModel: EditManagementViewModel,
    onClose: () -> Unit,
    onDeactivatePackage: () -> Unit,
    onAddMoreSection: () -> Unit,
    onGoToEditQuestion: (String, String, String, String, Int) -> Unit // (type, parentId, questionId, paketName, questionNumber)
) {
    // Observasi State dari ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current // [BARU] Untuk Toast

    // STATE popup edit section (UI State Lokal)
    var showEditSectionDialog by remember { mutableStateOf(false) }
    var selectedSectionForEdit by remember { mutableStateOf<TryoutSectionUiModel?>(null) }

    // STATE popup add section
    var showAddSectionDialog by remember { mutableStateOf(false) }

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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Edit ${paket.name}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF333333)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List section (Dynamic from Firestore)
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE91E63))
            }
        } else if (uiState.sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada section.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false), // Biarkan wrap content jika sedikit, scroll jika banyak
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.sections) { section ->
                    SectionCard(
                        section = section,
                        isEditable = !paket.isActive, // [BARU] Kirim status editable
                        onEditClick = { clicked ->
                            if (paket.isActive) {
                                Toast.makeText(context, "Nonaktifkan paket terlebih dahulu untuk mengedit.", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedSectionForEdit = clicked
                                showEditSectionDialog = true
                            }
                        },
                        onManageQuestionClick = { subtestId ->
                            // NAVIGASI KE LIST SOAL DENGAN MEMBAWA SUBTEST ID
                            onGoToEditQuestion(
                                "tryout",      // type
                                paket.id,      // parentId (tryoutId)
                                "list_soal|$subtestId",   // Pass ID here separated by pipe |
                                paket.name,    // paketName
                                section.questionCount              // questionNumber
                                // PENTING: Anda perlu memodifikasi Navigasi Anda
                                // agar bisa menerima parameter 'subtestId' tambahan
                                // atau selipkan di parameter yang ada jika malas ubah route.
                            ).apply {
                                // Cara terbaik: Tambahkan parameter ke-6 di callback onGoToEditQuestion
                                // onGoToEditQuestion(type, parentId, ..., subtestId)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tombol Edit Soal (umum, bukan per section)
//        OutlinedButton(
//            onClick = {
//                onGoToEditQuestion(
//                    "tryout",
//                    paket.id,
//                    "list_soal",
//                    paket.name,
//                    0
//                )
//            },
//            modifier = Modifier.align(Alignment.End),
//            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
//        ) {
//            Text("Edit Soal")
//        }

//        Spacer(modifier = Modifier.height(8.dp))

        // Tombol Add More Section
        OutlinedButton(
            onClick = {
                if (paket.isActive) {
                    Toast.makeText(context, "Nonaktifkan paket terlebih dahulu untuk menambah subtest.", Toast.LENGTH_SHORT).show()
                } else {
                    showAddSectionDialog = true
                }
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (paket.isActive) Color.LightGray else Color.Gray
            )
        ) {
            Text("Tambah Subtest")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Aktifkan/Nonaktifkan Paket
        Button(
            onClick = {
                if (paket.isActive) {
                    // Nonaktifkan (Langsung)
                    viewModel.deactivatePackage(paket.id) {
                        onDeactivatePackage() // Refresh parent
                        Toast.makeText(context, "Tryout Dinonaktifkan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Aktifkan (Pakai Validasi Target)
                    viewModel.activatePackage(
                        tryoutId = paket.id,
                        onSuccess = {
                            onDeactivatePackage() // Refresh parent
                            Toast.makeText(context, "Tryout Berhasil Diaktifkan", Toast.LENGTH_SHORT).show()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (paket.isActive) Color(0xFFE53935) else Color(0xFF4CAF50),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (paket.isActive) "Nonaktifkan Paket" else "Aktifkan Paket",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }

    // ---------- POPUP EDIT SECTION ----------
    if (showEditSectionDialog && selectedSectionForEdit != null) {
        val section = selectedSectionForEdit!!

        EditSectionDialog(
            tryoutId = paket.id,          // <-- ADDED: tryoutId
            sectionId = section.id,       // <-- ADDED: sectionId
            paketName = paket.name,
            sectionName = section.title,
            initialState = EditSectionUiState(
                type = section.type,
                subtest = section.title,
                timeMinutes = section.timeMinutes,
                questionCount = section.questionCount,
                topicsString = section.topicsString
            ),
            onDismiss = {
                showEditSectionDialog = false
                selectedSectionForEdit = null
                // Optional: Refresh sections list after edit
                viewModel.loadSections(paket.id)
            },
            // REMOVED: onSaveSection (Logic is now inside ViewModel)
            onEditSoalTryout = onGoToEditQuestion
        )
    }

    // ---------- POPUP ADD SECTION ----------
    if (showAddSectionDialog) {
        AddSectionDialog(
            tryoutId = paket.id,          // <-- ADDED: tryoutId
            paketName = paket.name,
            onDismiss = {
                showAddSectionDialog = false
                // Optional: Refresh sections list after add
                viewModel.loadSections(paket.id)
            },
            // REMOVED: onSaveSection (Logic is now inside ViewModel)
            onEditSoalTryout = onGoToEditQuestion
        )
    }
}

// -------------------------------
// CARD SATU SECTION (UI Murni)
// -------------------------------
@Composable
private fun SectionCard(
    section: TryoutSectionUiModel,
    isEditable: Boolean, // Parameter status
    onEditClick: (TryoutSectionUiModel) -> Unit,
    onManageQuestionClick: (String) -> Unit // Callback baru: ID Subtest
) {
    // Tentukan warna icon berdasarkan status editable
    val settingColor = if (isEditable) Color.Gray else Color.LightGray
    val listIconColor = Color(0xFF2196F3) // Selalu biru (Material Blue)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFE0E0E0),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121),
                    modifier = Modifier.weight(1f)
                )
                Row {
                    // Tombol kelola Soal Spesifik Subtest ini
                    IconButton(
                        onClick = {onManageQuestionClick(section.id)}, //// section.id disini adalah subtestId
                        modifier = Modifier.size(24.dp)
                    ) {
                        // Ubah icon jika tidak editable agar user tau ini "View Mode"
                        if (isEditable) {
                            Icon(Icons.Default.List, contentDescription = "Kelola Soal", tint = listIconColor)
                        } else {
                            Icon(Icons.Default.Visibility, contentDescription = "Lihat Soal", tint = listIconColor)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tombol Edit Section
                    IconButton(
                        onClick = { onEditClick(section) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit Section", tint = settingColor)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(label = "Tipe", value = section.type)
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(label = "Waktu", value = "${section.timeMinutes} menit")
            Spacer(modifier = Modifier.height(4.dp))

            val isTargetMet = section.actualCount >= section.questionCount
            val progressColor = if (isTargetMet) Color(0xFF4CAF50) else Color(0xFFE53935)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress Soal",
                    fontSize = 13.sp,
                    color = Color(0xFF616161),
                    modifier = Modifier.width(90.dp)
                )

                Surface(
                    color = progressColor,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        // Format: "5 / 20"
                        text = "${section.actualCount} / ${section.questionCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Opsional: Tambahkan teks status kecil
//                if (!isTargetMet) {
//                    Text(
//                        text = "(Belum Penuh)",
//                        fontSize = 11.sp,
//                        color = Color(0xFFE53935)
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val kisiKisiText = if (section.topicsString.isNotBlank()) section.topicsString else "-"

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Kisi-kisi",
                    fontSize = 13.sp,
                    color = Color(0xFF616161),
                    modifier = Modifier.width(90.dp) // Samakan lebar label dengan InfoRow
                )
                Text(
                    text = kisiKisiText,
                    fontSize = 13.sp, // Font sedikit lebih besar agar terbaca
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    lineHeight = 18.sp // Spasi antar baris jika panjang
                )
            }
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
        Text(text = label, fontSize = 13.sp, color = Color(0xFF616161), modifier = Modifier.width(90.dp))
        Surface(color = Color(0xFF9E9E9E), shape = RoundedCornerShape(10.dp)) {
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