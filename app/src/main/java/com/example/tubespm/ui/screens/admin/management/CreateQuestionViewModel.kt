package com.example.tubespm.ui.screens.admin.management

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.repository.ExerciseCatalogRepository
import com.example.tubespm.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CreateQuestionUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val questionText: String = "",
    val questionImageUri: Uri? = null,
    val questionImageUrl: String? = null,
    val answerMap: Map<String, String> = mapOf(
        "A" to "",
        "B" to "",
        "C" to "",
        "D" to "",
        "E" to ""
    ),
    val correctAnswer: String = "",
    val discussion: String = "",
    val subtestId: String = "",
    val topicId: String = "",
    val questionNumber: Int = 1,
    val error: String? = null,
    val isSavedSuccess: Boolean = false
)

class CreateQuestionViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateQuestionUiState())
    val uiState: StateFlow<CreateQuestionUiState> = _uiState.asStateFlow()

    // Fungsi inisialisasi data dari Navigasi
    fun initData(questionNumber: Int, subtestId: String?) {
        _uiState.update {
            it.copy(
                questionNumber = questionNumber,
                subtestId = subtestId ?: it.subtestId // Jangan timpa jika null
            )
        }
    }

    fun updateQuestionText(text: String) {
        _uiState.update { it.copy(questionText = text) }
    }

    fun updateQuestionImageUri(uri: Uri?) {
        _uiState.update { it.copy(questionImageUri = uri) }
    }

    fun updateAnswerOption(label: String, text: String) {
        _uiState.update {
            val newMap = it.answerMap.toMutableMap().apply { put(label, text) }
            it.copy(answerMap = newMap)
        }
    }

    fun setCorrectAnswer(label: String) {
        _uiState.update { it.copy(correctAnswer = label) }
    }

    fun updateDiscussion(text: String) {
        _uiState.update { it.copy(discussion = text) }
    }

    fun updateDiscussionText(text: String) {
        updateDiscussion(text)
    }

    fun updateSubtestId(subtestId: String) {
        _uiState.update { it.copy(subtestId = subtestId) }
    }

    fun updateTopicId(topicId: String) {
        _uiState.update { it.copy(topicId = topicId) }
    }

    fun updateQuestionNumber(number: Int) {
        _uiState.update { it.copy(questionNumber = number) }
    }

    /**
     * Simpan soal baru ke Firestore
     */
    fun createQuestion(
        context: Context, // ADDED: Context for image processing
        parentId: String,
        type: String, // "tryout" atau "latihan_soal"
        onSuccess: (String) -> Unit, // Callback dengan questionId
        onError: (String) -> Unit
    ) {
        val currentState = _uiState.value

        // Validasi
        if (currentState.questionText.isBlank()) {
            onError("Teks soal tidak boleh kosong")
            return
        }

        if (currentState.answerMap.values.any { it.isBlank() }) {
            onError("Semua opsi jawaban harus diisi")
            return
        }

        if (currentState.correctAnswer.isBlank()) {
            onError("Pilih jawaban yang benar")
            return
        }

        if (type == "tryout" && currentState.subtestId.isBlank()) {
            onError("Error Fatal: Subtest ID Hilang. Silakan kembali ke menu sebelumnya.")
            return // <--- WAJIB ADA: Agar kode di bawah tidak dijalankan
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Process Image to Base64 (Background Thread)
                val imageBase64: String? = if (currentState.questionImageUri != null) {
                    withContext(Dispatchers.IO) {
                        ImageUtils.uriToBase64(context, currentState.questionImageUri)
                    }
                } else {
                    null
                }

                // Konversi Map jawaban ke List
                val optionsList = listOf("A", "B", "C", "D", "E").map { label ->
                    currentState.answerMap[label] ?: ""
                }

                // Buat object QuizQuestion
                val newQuestion = QuizQuestion(
                    id = "", // Akan di-generate oleh repository
                    questionNumber = currentState.questionNumber,
                    subtestId = currentState.subtestId,
                    topicId = currentState.topicId,
                    questionText = currentState.questionText,
                    questionImage = imageBase64,
                    options = optionsList,
                    correctAnswer = currentState.correctAnswer,
                    discussion = currentState.discussion
                )

                // Simpan ke Firestore
                val questionId = repository.createQuestion(parentId, type, newQuestion)

//                Update questionCount di parent (Auto Increment)
                // Kita kirim 0 atau angka berapapun, karena di Repository kita pakai FieldValue.increment(1)
//                val currentCount = getCurrentQuestionCount(parentId, type)
                repository.updateQuestionCount(parentId, type, 0)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccess = true,
                        questionImageUri = null // Reset setelah upload
                    )
                }

                onSuccess(questionId)

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

    /**
     * Helper untuk mendapatkan current question count
     * (Sementara return 0, bisa di-improve dengan query)
     */
    private suspend fun getCurrentQuestionCount(parentId: String, type: String): Int {
        // TODO: Query actual count dari subcollection
        // Untuk sekarang return 0, akan di-increment
        return 0
    }



    // Reset form tapi pertahankan ID penting untuk soal berikutnya
    fun resetStateForNextQuestion(nextQuestionNumber: Int) {
        _uiState.update { current ->
            CreateQuestionUiState(
                subtestId = current.subtestId,
                topicId = current.topicId,
                questionNumber = nextQuestionNumber
            )
        }
    }

    fun resetState() {
        _uiState.value = CreateQuestionUiState()
    }
}