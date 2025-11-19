package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Section
import com.example.tubespm.data.model.Tryout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.util.UUID

// ==========================================
// ✅ PINDAHKAN DATA CLASS KE SINI
// ==========================================
data class EditSectionUiState(
    val type: String = "TPS",
    val subtest: String = "Penalaran Umum",
    val timeMinutes: Int = 20,
    val questionCount: Int = 20
)

class EditSectionViewModel : ViewModel() {
    private val db = Firebase.firestore

    /**
     * Menambahkan Section baru ke dalam Tryout
     */
    fun addSection(
        tryoutId: String,
        sectionData: EditSectionUiState, // Error merah akan hilang sekarang
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val newSection = Section(
            sectionId = UUID.randomUUID().toString().take(8), // Generate ID simple
            sectionName = sectionData.subtest,
            sectionDuration = sectionData.timeMinutes,
            sectionQuestionCount = sectionData.questionCount
            // subtests bisa diisi default atau kosong
        )

        updateTryoutSections(tryoutId, newSection, isAdd = true, onSuccess, onError)
    }

    /**
     * Mengupdate Section yang sudah ada
     */
    fun updateSection(
        tryoutId: String,
        oldSectionId: String,
        sectionData: EditSectionUiState,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Note: Firestore tidak bisa update array item secara parsial dengan mudah.
        // Kita harus baca dokumen, modifikasi list di memori, lalu tulis ulang listnya.
        // Fungsi helper `updateTryoutSections` akan menangani ini.

        val updatedSection = Section(
            sectionId = oldSectionId, // Pertahankan ID lama
            sectionName = sectionData.subtest,
            sectionDuration = sectionData.timeMinutes,
            sectionQuestionCount = sectionData.questionCount
        )

        updateTryoutSections(tryoutId, updatedSection, isAdd = false, onSuccess, onError)
    }

    private fun updateTryoutSections(
        tryoutId: String,
        sectionItem: Section,
        isAdd: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val tryoutRef = db.collection("tryouts").document(tryoutId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(tryoutRef)
                    val tryout = snapshot.toObject(Tryout::class.java) ?: throw Exception("Tryout not found")

                    val currentSections = tryout.sections.toMutableList()

                    if (isAdd) {
                        currentSections.add(sectionItem)
                    } else {
                        // Find index and replace
                        val index = currentSections.indexOfFirst { it.sectionId == sectionItem.sectionId }
                        if (index != -1) {
                            currentSections[index] = sectionItem
                        }
                    }

                    transaction.update(tryoutRef, "sections", currentSections)
                }.addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener {
                    onError(it.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error updating section")
            }
        }
    }
}