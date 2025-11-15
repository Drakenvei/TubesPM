package com.example.tubespm.ui.screens.siswa.pembahasan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuestionWithExplanation
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PembahasanUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val questions: List<QuestionWithExplanation> = emptyList()
)

@HiltViewModel
class PembahasanViewModel @Inject constructor(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle // untuk menerima argumen
) : ViewModel() {
    private val _uiState = MutableStateFlow(PembahasanUiState())
    val uiState = _uiState.asStateFlow()

    private val activityId: String = savedStateHandle.get<String>("activityId")!!

    init {
        loadPembahasanData()
    }

    private fun loadPembahasanData(){
        if (activityId.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, error = "Activity ID tidak valid")
            }
            return
        }
            viewModelScope.launch {
            try {
                // 1. Ambil data aktivitas (untuk mendapatkan ID tryout)
                val activity = repository.getActivity(activityId)
                if (activity == null) throw Exception("Aktivitas tidak ditemukan")

                // 2. Ambil semua soal dari bank soal (kunci jawaban & pembahasan)
                val allQuestions = repository.getTryoutQuestions(activity.activityRefId)

                // 3. Ambil semua jawaban user (hanya butuh data terakhir, jadi pakai .first())
                val userAnswers = repository.getSavedAnswers(activityId).first()

                // 4. Gabungkan (mapping) kedua data
                val combinedData: List<QuestionWithExplanation> = mapData(allQuestions,userAnswers)

                _uiState.update {
                    it.copy(isLoading = false, questions = combinedData)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.localizedMessage)
                }
            }
        }
    }

    /**
     * Helper untuk "menjahit" data soal dan data jawaban
     */

    private fun mapData(
        questions: List<QuizQuestion>,
        userAnswers: Map<String, String> // Map<QuestionID, AnswerString>
    ) : List<QuestionWithExplanation> {

        return questions.map { question ->
            val userAnswerString = userAnswers[question.id] // Jawaban user (misal: "B")

            // Konversi "A" -> 0, "B" -> 1, dst.
            val correctAnwerIndex = question.correctAnswer[0] - 'A'
            val userAnswerIndex = userAnswerString?.get(0)?.minus('A') // Bisa null

            QuestionWithExplanation(
                id = question.id,
                subtest = question.subtestId,
                questionText = question.questionText,
                options = question.options,
                explanation = question.discussion,
                correctAnswerIndex = correctAnwerIndex,
                userAnswerIndex = userAnswerIndex
            )
        }
    }
}