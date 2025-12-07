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
        sectionData: EditSectionUiState,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Buat subtest baru dengan data yang diinput
        val newSubtest = Subtest(
            subtestId = UUID.randomUUID().toString().take(8),
            subtestName = sectionData.subtest,
            duration = sectionData.timeMinutes,
            questionCount = sectionData.questionCount,
            topics = emptyList()
        )

        // Buat section baru dengan subtest
        val newSection = Section(
            sectionId = if (sectionData.type == "TPS") "tps" else "literasi",
            sectionName = sectionData.subtest,
            subtests = listOf(newSubtest)
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
        // Baca section yang ada, update subtest pertama, atau buat subtest baru jika belum ada
        viewModelScope.launch {
            try {
                val tryoutRef = db.collection("tryouts").document(tryoutId)
                
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(tryoutRef)
                    val tryout = snapshot.toObject(Tryout::class.java) ?: throw Exception("Tryout not found")
                    
                    val currentSections = tryout.sections.toMutableList()
                    val sectionIndex = currentSections.indexOfFirst { it.sectionId == oldSectionId }
                    
                    if (sectionIndex == -1) {
                        throw Exception("Section not found")
                    }
                    
                    val existingSection = currentSections[sectionIndex]
                    val existingSubtests = existingSection.subtests.toMutableList()
                    
                    // Update atau buat subtest pertama
                    val updatedSubtest = Subtest(
                        subtestId = existingSubtests.firstOrNull()?.subtestId ?: UUID.randomUUID().toString().take(8),
                        subtestName = sectionData.subtest,
                        duration = sectionData.timeMinutes,
                        questionCount = sectionData.questionCount,
                        topics = existingSubtests.firstOrNull()?.topics ?: emptyList()
                    )
                    
                    if (existingSubtests.isEmpty()) {
                        existingSubtests.add(updatedSubtest)
                    } else {
                        existingSubtests[0] = updatedSubtest
                    }
                    
                    // Update section dengan subtests yang baru
                    val updatedSection = existingSection.copy(
                        sectionName = sectionData.subtest,
                        subtests = existingSubtests
                    )
                    
                    currentSections[sectionIndex] = updatedSection
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