package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Section
import com.example.tubespm.data.model.Subtest
import com.example.tubespm.data.model.Topic
import com.example.tubespm.data.model.Tryout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class EditSectionUiState(
    val type: String = "TPS",
    val subtest: String = "Penalaran Umum",
    val timeMinutes: Int = 20,
    val questionCount: Int = 20,
    val topicsString: String = ""
)

class EditSectionViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val subtestIdMap = mapOf(
        "Penalaran Umum" to "pu",
        "Pengetahuan Kuantitatif" to "pk",
        "Pengetahuan dan Pemahaman Umum" to "ppu",
        "Pemahaman Bacaan dan Menulis" to "pbm",
        "Literasi dalam Bahasa Indonesia" to "lbi",
        "Literasi dalam Bahasa Inggris" to "lbing",
        "Penalaran Matematika" to "pm"
    )

    fun saveSubtest(
        tryoutId: String,
        subtestIdToEdit: String? = null,
        data: EditSectionUiState,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val tryoutRef = db.collection("tryouts").document(tryoutId)

            // Tentukan ID Baru berdasarkan Nama Baru
            val newSubtestId = subtestIdMap[data.subtest] ?: "umum_${System.currentTimeMillis()}"

            // Flag untuk cek apakah ID berubah (Hanya berlaku saat Edit)
            val isIdChanged = subtestIdToEdit != null && subtestIdToEdit != newSubtestId

            db.runTransaction { transaction ->
                val snapshot = transaction.get(tryoutRef)
                val tryout = snapshot.toObject(Tryout::class.java) ?: throw Exception("Tryout tidak ditemukan")

                // 1. Siapkan Data Target (Tujuan Baru)
                val targetSectionId = if (data.type.equals("TPS", ignoreCase = true)) "tps" else "literasi"
                val targetSectionName = if (targetSectionId == "tps") "Tes Potensi Skolastik" else "Literasi"

                // Ambil daftar section saat ini untuk dimodifikasi
                val currentSections = tryout.sections.toMutableList()

                val finalSubtestId = if (subtestIdToEdit != null) {
                    subtestIdToEdit
                } else {
                    // Cek apakah ID ini sudah dipakai di section ini? (Opsional, tapi aman ditimpa kalau logic benar)
                    subtestIdMap[data.subtest] ?: "umum_${System.currentTimeMillis()}"
                }

                val topicList = data.topicsString.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { topicName ->
                        // Buat ID topik simple (misal: "Aljabar Linear" -> "aljabar_linear")
                        val tId = topicName.lowercase().replace(" ", "_")
                        Topic(topicId = tId, name = topicName)
                    }

                val isDuplicate = currentSections.any() { section ->
                    section.subtests.any() {existingSubtest ->
                        if (existingSubtest.subtestId == newSubtestId) {
                            if (subtestIdToEdit == null) {
                                true
                            } else {
                                existingSubtest.subtestId != subtestIdToEdit
                            }
                        } else {
                            false
                        }
                    }
                }

                if (isDuplicate) {
                    throw Exception("Subtest '${data.subtest}' sudah ada di Tryout ini.")
                }
                // ============================
                // LOGIKA TAMBAH BARU (ADD)
                // ============================
                if (subtestIdToEdit == null) {

                    val newSubtest = Subtest(
                        subtestId = finalSubtestId, // Pakai ID standar (pu, pk...)
                        subtestName = data.subtest,
                        duration = data.timeMinutes,
                        questionCount = data.questionCount,
                        topics = topicList
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
                        subtestId = newSubtestId,
                        duration = data.timeMinutes,
                        questionCount = data.questionCount,
                        topics = topicList
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

                val newTotalDuration = currentSections.sumOf { sec ->
                    sec.subtests.sumOf { sub -> sub.duration }
                }

                val newTotalQuestionCount = currentSections.sumOf { sec ->
                    sec.subtests.sumOf { sub -> sub.questionCount }
                }

                // Simpan perubahan ke Firestore
                transaction.update(tryoutRef, mapOf(
                    "sections" to currentSections,
                    // Field ini sekarang akan TERTULIS di database
                    "totalDuration" to newTotalDuration,
                    "totalQuestionCount" to newTotalQuestionCount
                ))

            }.addOnSuccessListener {
                if (isIdChanged && subtestIdToEdit != null) {
                    migrateQuestions(tryoutId, subtestIdToEdit, newSubtestId, onSuccess, onError)
                } else {
                    onSuccess()
                }
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

    // Fungsi Migrasi Soal: Mencari semua soal dengan ID lama, ubah ke ID baru
    private fun migrateQuestions(
        tryoutId: String,
        oldSubtestId: String,
        newSubtestId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Cari semua soal yang punya subtestId lama
                val questionsRef = db.collection("tryouts").document(tryoutId).collection("questions")
                val snapshot = questionsRef.whereEqualTo("subtestId", oldSubtestId).get().await()

                if (!snapshot.isEmpty) {
                    // Siapkan batch
                    val batch = db.batch()

                    snapshot.documents.forEach { doc ->
                        batch.update(doc.reference, "subtestId", newSubtestId)
                    }

                    // Eksekusi Batch
                    batch.commit().await()
                }
                onSuccess()
            } catch (e: Exception) {
                onError("Parent sukses, tapi gagal migrasi soal: ${e.message}")
            }
        }
    }
}