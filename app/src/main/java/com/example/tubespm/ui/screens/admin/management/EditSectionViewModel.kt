package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Section
import com.example.tubespm.data.model.Subtest
import com.example.tubespm.data.model.Tryout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.util.UUID

data class EditSectionUiState(
    val type: String = "TPS",
    val subtest: String = "Penalaran Umum",
    val timeMinutes: Int = 20,
    val questionCount: Int = 20
)

class EditSectionViewModel : ViewModel() {
    private val db = Firebase.firestore

    fun saveSubtest(
        tryoutId: String,
        subtestIdToEdit: String? = null,
        data: EditSectionUiState,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val tryoutRef = db.collection("tryouts").document(tryoutId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(tryoutRef)
                val tryout = snapshot.toObject(Tryout::class.java) ?: throw Exception("Tryout tidak ditemukan")

                // 1. Siapkan Data Target (Tujuan Baru)
                val targetSectionId = if (data.type.equals("TPS", ignoreCase = true)) "tps" else "literasi"
                val targetSectionName = if (targetSectionId == "tps") "Tes Potensi Skolastik" else "Literasi"

                // Ambil daftar section saat ini untuk dimodifikasi
                val currentSections = tryout.sections.toMutableList()

                // ============================
                // LOGIKA TAMBAH BARU (ADD)
                // ============================
                if (subtestIdToEdit == null) {
                    val newSubtest = Subtest(
                        subtestId = UUID.randomUUID().toString(),
                        subtestName = data.subtest,
                        duration = data.timeMinutes,
                        questionCount = data.questionCount,
                        topics = emptyList()
                    )

                    // Masukkan ke section tujuan (Buat section jika belum ada)
                    addToSection(currentSections, targetSectionId, targetSectionName, newSubtest)
                }

                // ============================
                // LOGIKA EDIT (UPDATE / MOVE)
                // ============================
                else {
                    // A. Cari Lokasi ASLI Subtest (Sumber)
                    val sourceSectionIndex = currentSections.indexOfFirst { sec ->
                        sec.subtests.any { it.subtestId == subtestIdToEdit }
                    }

                    if (sourceSectionIndex == -1) {
                        throw Exception("Subtest asli tidak ditemukan. Mungkin sudah dihapus?")
                    }

                    val sourceSection = currentSections[sourceSectionIndex]
                    val subtestIndex = sourceSection.subtests.indexOfFirst { it.subtestId == subtestIdToEdit }
                    val existingSubtest = sourceSection.subtests[subtestIndex]

                    // B. Update Data Subtest
                    val updatedSubtest = existingSubtest.copy(
                        subtestName = data.subtest,
                        duration = data.timeMinutes,
                        questionCount = data.questionCount
                    )

                    // C. Cek Apakah Pindah Kategori? (Misal: TPS -> Literasi)
                    if (sourceSection.sectionId == targetSectionId) {
                        // KASUS 1: Tidak Pindah (Update di tempat)
                        val mutableSubtests = sourceSection.subtests.toMutableList()
                        mutableSubtests[subtestIndex] = updatedSubtest
                        currentSections[sourceSectionIndex] = sourceSection.copy(subtests = mutableSubtests)
                    } else {
                        // KASUS 2: Pindah Kategori (Move)

                        // 1. Hapus dari Section Lama
                        val sourceSubtests = sourceSection.subtests.toMutableList()
                        sourceSubtests.removeAt(subtestIndex)

                        // Jika section lama jadi kosong, opsional: bisa dihapus atau dibiarkan
                        currentSections[sourceSectionIndex] = sourceSection.copy(subtests = sourceSubtests)

                        // 2. Tambahkan ke Section Baru
                        addToSection(currentSections, targetSectionId, targetSectionName, updatedSubtest)
                    }
                }

                // Simpan perubahan ke Firestore
                transaction.update(tryoutRef, "sections", currentSections)

            }.addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { e ->
                onError(e.message ?: "Gagal menyimpan subtest")
            }
        }
    }

    // Helper untuk menambahkan subtest ke section list (menangani jika section belum ada)
    private fun addToSection(
        sectionsList: MutableList<Section>,
        targetId: String,
        targetName: String,
        subtest: Subtest
    ) {
        val targetIndex = sectionsList.indexOfFirst { it.sectionId == targetId }

        if (targetIndex != -1) {
            // Section sudah ada, tambahkan ke list
            val targetSection = sectionsList[targetIndex]
            val newSubtests = targetSection.subtests.toMutableList()
            newSubtests.add(subtest)
            sectionsList[targetIndex] = targetSection.copy(subtests = newSubtests)
        } else {
            // Section belum ada, buat baru
            val newSection = Section(
                sectionId = targetId,
                sectionName = targetName,
                subtests = listOf(subtest)
            )
            sectionsList.add(newSection)
        }
    }
}