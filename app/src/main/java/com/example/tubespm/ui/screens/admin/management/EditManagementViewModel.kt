package com.example.tubespm.ui.screens.admin.management

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Model UI khusus untuk list di dalam Dialog
data class TryoutSectionUiModel(
    val id: String, // subtestId (Bukan sectionId!)
    val title: String, // subtestName
    val type: String, // "TPS" atau "Literasi" (Diambil dari parent section)
    val timeMinutes: Int, // duration
    val questionCount: Int, // questionCount
    val parentSectionId: String, // "tps" atau "literasi" (Penting untuk referensi)
    val topicsString: String = "" //untuk menampung kisi-kisi
)

data class EditManagementUiState(
    val isLoading: Boolean = true,
    val sections: List<TryoutSectionUiModel> = emptyList(), //Ini sebenarnya list of Subtests
    val error: String? = null
)

class EditManagementViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditManagementUiState())
    val uiState: StateFlow<EditManagementUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore

    /**
     * Mengambil detail sections dari dokumen Tryout spesifik
     */
    fun loadSections(tryoutId: String) {
        _uiState.update { it.copy(isLoading = true) }

        db.collection("tryouts").document(tryoutId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val tryout = snapshot.toObject(Tryout::class.java)

                        // --- FLATTEING DATA ---
                        // Mengubah struktur nested (Section -> Subtests) menjadi satu list datar
                        val flatList = mutableListOf<TryoutSectionUiModel>()

                        tryout?.sections?.forEach { section ->
                            // Logic to determine type label (TPS/Literasi)
                            val typeLabel = if (section.sectionId.equals("tps", ignoreCase = true)) "TPS" else "Literasi"

                            section.subtests.forEach { subtest ->

                                // Proses Mapping Topics ke String
                                // Mengubah List<Topic> menjadi String "Aljabar, Geometri"
                                val topicsStr = subtest.topics.joinToString(", ") { it.name }

                                flatList.add(
                                    TryoutSectionUiModel(
                                        // ERROR IS LIKELY HERE:
                                        // Ensure you are using 'subtest.subtestId' (e.g., "pu"), NOT 'section.sectionId' (e.g., "tps")
                                        id = subtest.subtestId,
                                        title = subtest.subtestName,
                                        type = typeLabel,
                                        timeMinutes = subtest.duration,
                                        questionCount = subtest.questionCount,
                                        parentSectionId = section.sectionId,
                                        topicsString = topicsStr
                                    )
                                )
                            }
                        }

                        _uiState.update {
                            it.copy(isLoading = false, sections = flatList, error = null)
                        }
                    } catch (err: Exception) {
                        Log.e("EditManagementVM", "Error parsing data", err)
                        _uiState.update { it.copy(isLoading = false, error = "Data corrupt") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Dokumen tidak ditemukan") }
                }
            }
    }

    /**
     * Mengubah status tryout menjadi inactive
     */
    fun deactivatePackage(tryoutId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            db.collection("tryouts").document(tryoutId)
                .update("status", "inactive")
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("EditManagementVM", "Gagal update status", e)
                }
        }
    }

    /**
     * Mengubah status tryout menjadi active
     */
    fun activatePackage(tryoutId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            db.collection("tryouts").document(tryoutId)
                .update("status", "active")
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("EditManagementVM", "Gagal activate status", e)
                }
        }
    }
}