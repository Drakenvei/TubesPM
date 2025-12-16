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
    val questions: List<QuestionWithExplanation> = emptyList(),
    val activityId: String = "" // field ini sudah ada
)

@HiltViewModel
class PembahasanViewModel @Inject constructor(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle // untuk menerima argumen
) : ViewModel() {
    private val _uiState = MutableStateFlow(PembahasanUiState())
    val uiState = _uiState.asStateFlow()

    // FIX: Ambil activityId dengan aman (default ke String kosong jika null)
    private val activityIdNav: String = savedStateHandle.get<String>("activityId") ?: ""

    init {
        loadPembahasanData()
    }

    private fun loadPembahasanData(){
        // Validasi ID sebelum fetching data
        if (activityIdNav.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Kesalahan Navigasi: Activity ID tidak ditemukan."
                )
            }
            return
        }

        // Simpan activityId yang valid ke UI State agar PembahasanScreen bisa membacanya
        _uiState.update { it.copy(activityId = activityIdNav) }

        viewModelScope.launch {
            try {
                val userActivity = repository.getActivity(activityIdNav)
                    ?: throw Exception("Aktivitas pengguna tidak ditemukan.")

                // NOTE: getQuestions() di interface mengembalikan List<QuizQuestion>,
                // jadi langsung assign tanpa .first()
                val questions: List<QuizQuestion> = repository.getQuestions(
                    refId = userActivity.activityRefId,
                    type = userActivity.type
                )

                // Tentukan urutan ID subtest sesuai urutan pengerjaan (Flow SNBT)
                val subtestPriority = listOf(
                    "pu",    // 1. Penalaran Umum
                    "ppu",   // 2. Pengetahuan & Pemahaman Umum
                    "pbm",   // 3. Pemahaman Bacaan & Menulis
                    "pk",    // 4. Pengetahuan Kuantitatif
                    "lbi",   // 5. Literasi Bhs Indonesia
                    "lbing", // 6. Literasi Bhs Inggris
                    "pm"     // 7. Penalaran Matematika
                )

                // Lakukan sorting
                val sortedQuestions = questions.sortedWith(
                    compareBy(
                        // Prioritas 1: Berdasarkan urutan list di atas
                        {
                            val id = it.subtestId.lowercase()
                            val index = subtestPriority.indexOf(id)
                            // Jika subtest tidak ada di list (misal typo), taruh di paling belakang (Int.MAX_VALUE)
                            if (index == -1) Int.MAX_VALUE else index
                        },
                        // Prioritas 2: Berdasarkan Nomor Soal
                        { it.questionNumber }
                    )
                )

                // getSavedAnswers() mengembalikan Flow<Map<...>>, jadi ambil first() dari Flow
                val userAnswers = repository.getSavedAnswers(activityIdNav).first()

                // Sekarang types cocok: mapData(List<QuizQuestion>, Map<String, String>)
                val combinedData = mapData(sortedQuestions, userAnswers)

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
            val correctAnwerIndex = question.correctAnswer.firstOrNull()?.minus('A') ?: 0
            val userAnswerIndex = userAnswerString?.firstOrNull()?.minus('A') // Bisa null

            QuestionWithExplanation(
                id = question.id,
                subtest = question.subtestId.uppercase(),
                questionText = question.questionText,
                questionImage = question.questionImage,
                options = question.options,
                optionImages = question.optionImages,
                explanation = question.discussion,
                explanationImage = question.explanationImage,
                correctAnswerIndex = correctAnwerIndex,
                userAnswerIndex = userAnswerIndex
            )
        }
    }
}