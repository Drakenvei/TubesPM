package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuizQuestion
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ListSoalUiState(
    val isLoading: Boolean = true,
    val questions: List<QuizQuestion> = emptyList(),
    val error: String? = null,
    val currentSubtestId: String? = null, // SubtestId untuk section yang sedang dilihat
    val isEditable: Boolean = true
)

class ListSoalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ListSoalUiState())
    val uiState: StateFlow<ListSoalUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore

    // Helper untuk cek status parent
    private suspend fun checkIsEditable(parentId: String, type: String): Boolean {
        return try {
            val collection = if (type == "tryout") "tryouts" else "latihan_soal"
            val doc = db.collection(collection).document(parentId).get().await()
            val status = doc.getString("status") ?: "inactive"
            status != "active" // Editable jika TIDAK active
        } catch (e: Exception) {
            true // Default aman
        }
    }

    /**
     * Load semua soal dari parent (tryout atau latihan_soal)
     */
    fun loadQuestions(parentId: String, type: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val editable = checkIsEditable(parentId, type)
                val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"
                val snapshot = db.collection(collectionName)
                    .document(parentId)
                    .collection("questions")
                    .orderBy("questionNumber")
                    .get()
                    .await()

                val questions = snapshot.toObjects(QuizQuestion::class.java)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questions = questions,
                        isEditable = editable,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat soal: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load soal berdasarkan section/subtest
     */
    fun loadQuestionsBySubtest(parentId: String, type: String, subtestId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            android.util.Log.d("DEBUG_SOAL", "Mencari soal di Parent: $parentId")
            android.util.Log.d("DEBUG_SOAL", "Dengan Subtest ID: '$subtestId'") // Pakai tanda kutip untuk cek spasi
            try {
                android.util.Log.d("DEBUG_SOAL", "Mencari soal di Parent: $parentId")
                android.util.Log.d("DEBUG_SOAL", "Dengan Subtest ID: '$subtestId'") // Pakai tanda kutip untuk cek spasi

                val editable = checkIsEditable(parentId, type)
                val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"
                val snapshot = db.collection(collectionName)
                    .document(parentId)
                    .collection("questions")
                    .whereEqualTo("subtestId", subtestId)
                    .orderBy("questionNumber")
                    .get()
                    .await()

                val questions = snapshot.toObjects(QuizQuestion::class.java)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questions = questions,
                        isEditable = editable,
                        error = null,
                        currentSubtestId = subtestId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Gagal memuat soal: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load soal berdasarkan section (load semua soal, filter di UI berdasarkan subtestId di section)
     */
    fun loadQuestionsBySection(parentId: String, type: String, subtestId: String) {
//        _uiState.update { it.copy(isLoading = true) }
//        viewModelScope.launch {
//            try {
//                val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"
//                val snapshot = db.collection(collectionName)
//                    .document(parentId)
//                    .collection("questions")
//                    .whereEqualTo("subtestId", subtestId)
//                    .orderBy("questionNumber")
//                    .get()
//                    .await()
//
//                val questions = snapshot.toObjects(QuizQuestion::class.java)
//
//                // UPDATE DISINI: Simpan currentSubtestId ke state!
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        questions = questions,
//                        error = null,
//                        currentSubtestId = subtestId // <--- PENTING!
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "Gagal memuat soal: ${e.message}"
//                    )
//                }
//            }
//        }
        loadQuestionsBySubtest(parentId, type, subtestId)
    }

    /**
     * Specialized loader for Latihan Soal to fetch parent metadata first
     */
    fun loadLatihanSoalQuestions(latihanId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // 1. Fetch Latihan Metadata to get subtestId
                val latihanDoc = db.collection("latihan_soal").document(latihanId).get().await()
                val latihan = latihanDoc.toObject(com.example.tubespm.data.model.LatihanSoal::class.java)
                val parentSubtestId = latihan?.subtestId // e.g., "pu"
                val status = latihanDoc.getString("status") ?: "inactive"

                val editable = status != "active"

                // 2. Fetch Questions
                val snapshot = db.collection("latihan_soal")
                    .document(latihanId)
                    .collection("questions")
                    .orderBy("questionNumber")
                    .get()
                    .await()

                val questions = snapshot.toObjects(QuizQuestion::class.java)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questions = questions,
                        isEditable = editable,
                        error = null,
                        currentSubtestId = parentSubtestId // <--- STORE THIS IN STATE
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteQuestionSingle(
        parentId: String,
        type: String,
        questionId: String,
        currentSubtestId: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"
                val parentRef = db.collection(collectionName).document(parentId)

                // HANYA HAPUS 1 DOKUMEN (Hemat Biaya & Cepat)
                db.collection(collectionName)
                    .document(parentId)
                    .collection("questions")
                    .document(questionId)
                    .delete()
                    .await()

                if (type == "latihan_soal") {
                    parentRef.update("questionCount", FieldValue.increment(-1))
                }

                // 2. [PENTING] REFRESH DATA AGAR UI UPDATE
                // Cek state: apakah kita sedang melihat subtest tertentu atau semua?
                val currentSubtest = _uiState.value.currentSubtestId

                if (type == "latihan_soal") {
                    loadLatihanSoalQuestions(parentId)
                } else if (currentSubtestId != null && currentSubtestId.isNotBlank()) {
                    // JIKA SUBTEST ID ADA -> LOAD PER SUBTEST (Filter)
                    loadQuestionsBySubtest(parentId, type, currentSubtestId)
                } else {
                    loadQuestions(parentId, type)
                }

                // ... (Update total question count di parent jika perlu) ...

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false, // Matikan loading
                        error = "Gagal menghapus: ${e.message}"
                    )
                }
            }
        }
    }
}

