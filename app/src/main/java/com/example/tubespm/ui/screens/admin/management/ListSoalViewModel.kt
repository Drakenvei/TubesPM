package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuizQuestion
import com.google.firebase.Firebase
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
    val error: String? = null
)

class ListSoalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ListSoalUiState())
    val uiState: StateFlow<ListSoalUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore

    /**
     * Load semua soal dari parent (tryout atau latihan_soal)
     */
    fun loadQuestions(parentId: String, type: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
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
            try {
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
     * Load soal berdasarkan section (load semua soal, filter di UI berdasarkan subtestId di section)
     */
    fun loadQuestionsBySection(parentId: String, type: String, sectionIdOrName: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"
                
                // Ambil tryout untuk mendapatkan subtestId dari section
                val tryoutDoc = db.collection(collectionName).document(parentId).get().await()
                val tryout = tryoutDoc.toObject(com.example.tubespm.data.model.Tryout::class.java)
                
                // Cari section berdasarkan sectionId atau sectionName
                val section = tryout?.sections?.find { 
                    it.sectionId == sectionIdOrName || it.sectionName == sectionIdOrName 
                }
                
                val subtestIds = section?.subtests?.map { it.subtestId } ?: emptyList()
                
                if (subtestIds.isEmpty()) {
                    // Jika tidak ada subtest, load semua soal
                    loadQuestions(parentId, type)
                    return@launch
                }
                
                // Load semua soal dan filter berdasarkan subtestId
                val allQuestions = db.collection(collectionName)
                    .document(parentId)
                    .collection("questions")
                    .orderBy("questionNumber")
                    .get()
                    .await()
                    .toObjects(com.example.tubespm.data.model.QuizQuestion::class.java)
                
                val filteredQuestions = allQuestions.filter { it.subtestId in subtestIds }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questions = filteredQuestions,
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
}
