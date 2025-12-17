package com.example.tubespm.ui.screens.admin.management

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.utils.ImageUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// State UI untuk Halaman Edit Soal
data class EditQuestionUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val questionData: QuizQuestion = QuizQuestion(),
    val answerMap: Map<String, String> = mapOf("A" to "", "B" to "", "C" to "", "D" to "", "E" to ""),
    val questionImageUri: Uri? = null, // URI untuk gambar baru yang dipilih
    val optionImageUris: Map<String, Uri?> = mapOf("A" to null, "B" to null, "C" to null, "D" to null, "E" to null),
    val explanationImageUri: Uri? = null,
    val error: String? = null,
    val isSavedSuccess: Boolean = false,
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

                    // Pastikan list optionImages memiliki ukuran 5 (isi null jika kurang)
                    val safeOptionImages = question.optionImages.toMutableList()
                    while (safeOptionImages.size < 5) safeOptionImages.add(null)
                    val safeQuestion = question.copy(optionImages = safeOptionImages)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            questionData = question,
                            answerMap = optionsMap,
                            questionImageUri = null,
                            explanationImageUri = null,
                            optionImageUris = mapOf("A" to null, "B" to null, "C" to null, "D" to null, "E" to null)
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
    fun updateExplanationImageUri(uri: Uri?) {
        _uiState.update { it.copy(explanationImageUri = uri) }
    }
    fun updateOptionImageUri(label: String, uri: Uri?) {
        _uiState.update {
            val newMap = it.optionImageUris.toMutableMap().apply { put(label, uri) }
            it.copy(optionImageUris = newMap)
        }
    }

    fun deleteQuestionImage() {
        _uiState.update {
            it.copy(
                questionImageUri = null,
                questionData = it.questionData.copy(questionImage = null)
            )
        }
    }

    fun deleteExplanationImage() {
        _uiState.update {
            it.copy(
                explanationImageUri = null,
                questionData = it.questionData.copy(explanationImage = null)
            )
        }
    }

    fun deleteOptionImage(label: String) {
        // 1. Reset URI
        updateOptionImageUri(label, null)

        // 2. Reset Data Model (Existing URL/Base64)
        _uiState.update { state ->
            val currentList = state.questionData.optionImages.toMutableList()
            val index = when(label) { "A"->0 "B"->1 "C"->2 "D"->3 "E"->4 else->-1 }
            if (index != -1 && index < currentList.size) {
                currentList[index] = null
                state.copy(questionData = state.questionData.copy(optionImages = currentList))
            } else state
        }
    }

    /**
     * Simpan perubahan ke Firestore
     */
    fun saveQuestion(
        context: Context,
        parentId: String,
        questionId: String,
        type: String = "tryout",
        onError: (String) -> Unit
    ) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val currentState = _uiState.value

                // 1. Handle Gambar Utama
                // Jika ada URI baru -> Convert ke Base64. Jika tidak -> Pakai data lama (bisa null jika dihapus).
                val finalQuestionImage: String? = if (currentState.questionImageUri != null) {
                    withContext(Dispatchers.IO) {
                        ImageUtils.uriToBase64(context, currentState.questionImageUri)
                    }
                } else {
                    currentState.questionData.questionImage
                }

                // 2. Handle Gambar Pembahasan
                val finalExplanationImage: String? = if (currentState.explanationImageUri != null) {
                    withContext(Dispatchers.IO) {
                        ImageUtils.uriToBase64(context, currentState.explanationImageUri)
                    }
                } else {
                    currentState.questionData.explanationImage
                }

                // 3. Handle Gambar Opsi
                val finalOptionImages = currentState.questionData.optionImages.toMutableList()
                val labels = listOf("A", "B", "C", "D", "E")

                labels.forEachIndexed { index, label ->
                    val uri = currentState.optionImageUris[label]
                    if (uri != null) {
                        val base64 = withContext(Dispatchers.IO) {
                            ImageUtils.uriToBase64(context, uri)
                        }
                        finalOptionImages[index] = base64
                    }
                    // Jika uri null, biarkan nilai lama di finalOptionImages (yang sudah di-handle oleh deleteOptionImage jika dihapus)
                }
                
                // Konversi Map jawaban kembali ke List
                val optionsList = listOf("A", "B", "C", "D", "E").map { label ->
                    currentState.answerMap[label] ?: ""
                }

                val updatedQuestion = currentState.questionData.copy(
                    options = optionsList,
                    questionImage = finalQuestionImage,
                    explanationImage = finalExplanationImage,
                    optionImages = finalOptionImages
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
                        questionImageUri = null,
                        explanationImageUri = null,
                        optionImageUris = mapOf("A" to null, "B" to null, "C" to null, "D" to null, "E" to null)
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Gagal menyimpan: ${e.message}") }
                onError(e.message ?: "Error")
            }
        }
    }
}