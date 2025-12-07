package com.example.tubespm.ui.screens.admin.management

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.utils.ImageUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// State UI untuk Halaman Edit Soal
data class EditQuestionUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val questionData: QuizQuestion = QuizQuestion(),
    val answerMap: Map<String, String> = mapOf("A" to "", "B" to "", "C" to "", "D" to "", "E" to ""),
    val questionImageUri: Uri? = null, // URI untuk gambar baru yang dipilih
    val error: String? = null,
    val isSavedSuccess: Boolean = false
)

class EditQuestionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditQuestionUiState())
    val uiState: StateFlow<EditQuestionUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore

    /**
     * Mengambil data soal berdasarkan Parent ID dan Question ID
     * (Asumsi struktur: tryouts/{id}/questions/{qid} atau latihan_soal/{id}/questions/{qid})
     */
    fun loadQuestion(parentId: String, questionId: String, type: String = "tryout") {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"
                val docRef = db.collection(collectionName).document(parentId)
                    .collection("questions").document(questionId)

                val snapshot = docRef.get().await()
                if (snapshot.exists()) {
                    val question = snapshot.toObject(QuizQuestion::class.java) ?: QuizQuestion()

                    // Mapping list options ke map A, B, C, D, E
                    val optionsMap = mutableMapOf<String, String>()
                    val labels = listOf("A", "B", "C", "D", "E")
                    labels.forEachIndexed { index, label ->
                        optionsMap[label] = question.options.getOrElse(index) { "" }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            questionData = question,
                            answerMap = optionsMap
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Soal tidak ditemukan") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Update text inputan (Soal, Pembahasan, Jawaban)
     */
    fun updateQuestionText(text: String) {
        _uiState.update { it.copy(questionData = it.questionData.copy(questionText = text)) }
    }

    fun updateDiscussionText(text: String) {
        _uiState.update { it.copy(questionData = it.questionData.copy(discussion = text)) }
    }

    fun updateAnswerOption(label: String, text: String) {
        _uiState.update {
            val newMap = it.answerMap.toMutableMap().apply { put(label, text) }
            it.copy(answerMap = newMap)
        }
    }

    fun setCorrectAnswer(label: String) {
        _uiState.update { it.copy(questionData = it.questionData.copy(correctAnswer = label)) }
    }

    fun updateQuestionImageUri(uri: Uri) {
        _uiState.update { it.copy(questionImageUri = uri) }
    }

    /**
     * Simpan perubahan ke Firestore
     */
    fun saveQuestion(parentId: String, questionId: String, type: String = "tryout") {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                
                // Upload gambar jika ada URI baru
                var imageUrl: String? = currentState.questionData.questionImage
                if (currentState.questionImageUri != null) {
                    imageUrl = ImageUtils.uploadImageToFirebaseStorage(currentState.questionImageUri)
                }
                
                // Konversi Map jawaban kembali ke List
                val optionsList = listOf("A", "B", "C", "D", "E").map { label ->
                    currentState.answerMap[label] ?: ""
                }

                val updatedQuestion = currentState.questionData.copy(
                    options = optionsList,
                    questionImage = imageUrl
                )

                val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"
                db.collection(collectionName).document(parentId)
                    .collection("questions").document(questionId)
                    .set(updatedQuestion)
                    .await()

                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        isSavedSuccess = true,
                        questionImageUri = null // Reset setelah upload
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Gagal menyimpan: ${e.message}") }
            }
        }
    }
}