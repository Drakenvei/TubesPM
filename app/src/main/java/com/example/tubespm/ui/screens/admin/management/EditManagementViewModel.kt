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
    val id: String, // biasanya sectionId (misal "tps")
    val title: String,
    val type: String, // "TPS" atau "Literasi"
    val timeMinutes: Int,
    val questionCount: Int
)

data class EditManagementUiState(
    val isLoading: Boolean = true,
    val sections: List<TryoutSectionUiModel> = emptyList(),
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
                        val uiSections = tryout?.sections?.map { sec ->
                            // Logika menentukan Tipe (TPS/Literasi) berdasarkan ID atau Nama
                            val type = if (sec.sectionId.contains("literasi", true) || sec.sectionName.contains("literasi", true)) {
                                "Literasi"
                            } else {
                                "TPS"
                            }

                            TryoutSectionUiModel(
                                id = sec.sectionId, // Menggunakan sectionId dari data model
                                title = sec.sectionName,
                                type = type,
                                timeMinutes = sec.sectionDuration,
                                questionCount = sec.sectionQuestionCount
                            )
                        } ?: emptyList()

                        _uiState.update {
                            it.copy(isLoading = false, sections = uiSections, error = null)
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