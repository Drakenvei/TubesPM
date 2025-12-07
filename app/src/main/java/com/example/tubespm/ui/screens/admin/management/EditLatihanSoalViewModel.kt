package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EditLatihanSoalUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val code: String = "",
    val title: String = "",
    val subtest: String = "",
    val status: String = "active",
    val error: String? = null,
    val isSavedSuccess: Boolean = false
)

class EditLatihanSoalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditLatihanSoalUiState())
    val uiState: StateFlow<EditLatihanSoalUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore

    fun loadLatihanSoal(latihanId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val doc = db.collection("latihan_soal").document(latihanId).get().await()
                val latihan = doc.toObject(LatihanSoal::class.java)

                if (latihan != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            code = latihan.code,
                            title = latihan.title,
                            subtest = latihan.subtest,
                            status = latihan.status,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Latihan soal tidak ditemukan"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat data: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateCode(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateSubtest(subtest: String) {
        _uiState.update { it.copy(subtest = subtest) }
    }

    fun updateStatus(status: String) {
        _uiState.update { it.copy(status = status) }
    }

    fun saveLatihanSoal(latihanId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentState = _uiState.value

        if (currentState.code.isBlank()) {
            onError("Kode tidak boleh kosong")
            return
        }

        if (currentState.title.isBlank()) {
            onError("Judul tidak boleh kosong")
            return
        }

        if (currentState.subtest.isBlank()) {
            onError("Subtest tidak boleh kosong")
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                db.collection("latihan_soal").document(latihanId)
                    .update(
                        mapOf(
                            "code" to currentState.code,
                            "title" to currentState.title,
                            "subtest" to currentState.subtest,
                            "status" to currentState.status
                        )
                    )
                    .await()

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccess = true
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Gagal menyimpan: ${e.message}"
                    )
                }
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
