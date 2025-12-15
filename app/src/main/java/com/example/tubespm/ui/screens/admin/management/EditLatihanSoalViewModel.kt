package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Topic
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
    val isStatusUpdating: Boolean = false,
    val code: String = "",
    val title: String = "",
    val subtest: String = "",
    val subtestId: String = "", // Simpan ID
    val topicsString: String = "", // Simpan string kisi-kisi
    val status: String = "active",
    val error: String? = null,
    val isSavedSuccess: Boolean = false
)

class EditLatihanSoalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditLatihanSoalUiState())
    val uiState: StateFlow<EditLatihanSoalUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore

    private val subtestMap = mapOf(
        "Penalaran Umum" to "pu",
        "Pengetahuan Kuantitatif" to "pk",
        "Pengetahuan dan Pemahaman Umum" to "ppu",
        "Pemahaman Bacaan dan Menulis" to "pbm",
        "Literasi dalam Bahasa Indonesia" to "lbi",
        "Literasi dalam Bahasa Inggris" to "lbing",
        "Penalaran Matematika" to "pm"
    )

    fun loadLatihanSoal(latihanId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val doc = db.collection("latihan_soal").document(latihanId).get().await()
                val latihan = doc.toObject(LatihanSoal::class.java)

                if (latihan != null) {
                    val topicsStr = latihan.topics.joinToString(", ") { it.name }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            code = latihan.code,
                            title = latihan.title,
                            subtest = latihan.subtest,
                            subtestId = latihan.subtestId,
                            topicsString = topicsStr,
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

    fun updateSubtest(subtestName: String) {
        val id = subtestMap[subtestName] ?: "umum"
        _uiState.update { it.copy(subtest = subtestName, subtestId = id) }
    }

    fun updateTopicsString(text: String) {
        _uiState.update { it.copy(topicsString = text) }
    }

    fun toggleLatihanStatus(latihanId: String) {
        val currentStatus = _uiState.value.status
        val newStatus = if (currentStatus == "active") "inactive" else "active"

        _uiState.update { it.copy(isStatusUpdating = true) }

        viewModelScope.launch {
            try {
                // Update langsung ke Firestore
                db.collection("latihan_soal").document(latihanId)
                    .update("status", newStatus)
                    .await()

                // Jika sukses, baru update UI
                _uiState.update {
                    it.copy(
                        status = newStatus,
                        isStatusUpdating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStatusUpdating = false,
                        error = "Gagal mengubah status: ${e.message}"
                    )
                }
            }
        }
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

        val newSubtestId = subtestMap[currentState.subtest] ?: "umum"

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                // Parsing String -> List<Topic>
                val topicList = currentState.topicsString.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { name ->
                        Topic(topicId = name.lowercase().replace(" ", "_"), name = name)
                    }

                db.collection("latihan_soal").document(latihanId)
                    .update(
                        mapOf(
                            "code" to currentState.code,
                            "title" to currentState.title,
                            "subtest" to currentState.subtest,
                            "subtestId" to currentState.subtestId,
                            "topics" to topicList,
                            "status" to currentState.status
                        )
                    )
                    .await()

                // Karena Latihan Soal hanya punya 1 jenis subtest, kita update semua soal di dalamnya
                migrateLatihanQuestions(latihanId, newSubtestId)

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

    // Fungsi Soft Delete Latihan Soal
    fun deleteLatihanSoal(latihanId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Soft delete: Ubah status menjadi 'deleted'
                db.collection("latihan_soal").document(latihanId)
                    .update("status", "deleted")
                    .await()

                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                onError("Gagal menghapus latihan soal: ${e.message}")
            }
        }
    }

    private suspend fun migrateLatihanQuestions(latihanId: String, newSubtestId: String) {
        val questionsRef = db.collection("latihan_soal").document(latihanId).collection("questions")

        // Ambil semua soal (limitasi batch 500, jika >500 perlu logic loop, tapi asumsi soal <500)
        val snapshot = questionsRef.get().await()

        if (!snapshot.isEmpty) {
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                // Cek dulu biar hemat write, kalau ID sudah sama tidak perlu update
                val currentSubId = doc.getString("subtestId")
                if (currentSubId != newSubtestId) {
                    batch.update(doc.reference, "subtestId", newSubtestId)
                }
            }
            // Commit jika ada operasi
            batch.commit().await()
        }
    }
}



