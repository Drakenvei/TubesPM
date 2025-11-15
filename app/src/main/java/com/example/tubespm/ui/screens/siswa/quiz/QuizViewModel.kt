package com.example.tubespm.ui.screens.siswa.quiz

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class QuizUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val questions: List<QuizQuestion> = emptyList(),
    val userAnswers: Map<String, String> = emptyMap(), // Map<QuestionID, AnswerString>
    val flaggedQuestions: Set<String> = emptySet(), // Set<QuestionID>
    val currentQuestionIndex: Int = 0,
    val subtestName: String = "Tryout", // Akan di-update
    val remainingTimeInSeconds: Long = 3600L,
    val quizMode: QuizMode = QuizMode.TRYOUT,
    val deadline: Date? = null // <-- Simpan deadline di state
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    private val activityId: String = savedStateHandle.get<String>("activityId")!!
    private var timerJob: Job? = null

    init {
        loadQuizSession()
    }

    private fun loadQuizSession() {
        if (activityId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Activity ID tidak valid") }
            return
        }

        viewModelScope.launch {
            try {
                // 1. Ambil data aktivitas & metadata
                val activity = repository.getActivity(activityId)
                if (activity == null) throw Exception("Aktivitas tidak ditemukan")

                val tryout = repository.getTryoutMetadata(activity.activityRefId)
                if (tryout == null) throw Exception("Tryout tidak ditemukan")

                // 2. Ubah status menjadi "in_progress"
                val deadline: Date
                if (activity.status == "not_started" || activity.deadline == null) {
                    // 1. Jika baru mulai, SET deadline di Firestore
                    deadline = repository.startQuizSession(activityId, tryout.totalDuration.toLong())
                } else {
                    // 2. Jika dilanjutkan, AMBIL deadline yang ada
                    deadline = activity.deadline!!
                }

                // 3. Hitung sisa waktu
                val initialRemaining = (deadline.time - System.currentTimeMillis()) / 1000
                if (initialRemaining <= 0) {
                    // Waktu sudah habis, submit paksa
                    submitQuiz()
                    return@launch
                }

                // 4. Ambil soal
                val questions = repository.getTryoutQuestions(activity.activityRefId)
                if (questions.isEmpty()) throw Exception("Soal tidak ditemukan")

                // 5. Update state awal
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questions = questions,
                        subtestName = tryout.title,
                        remainingTimeInSeconds = initialRemaining, // Set sisa waktu
                        deadline = deadline // Simpan deadline
                    )
                }

                // 6. Mulai timer
                startTimer()

                // 7. Dengarkan perubahan jawaban
                listenForSavedAnswers()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun listenForSavedAnswers() {
        viewModelScope.launch {
            repository.getSavedAnswers(activityId)
                .distinctUntilChanged() // <-- Hanya jika ada jawaban BARU
                .collect { answers ->
                    _uiState.update {
                        it.copy(
                            userAnswers = answers,
                        )
                    }
                    // Update jumlah jawaban di Firestore
                    updateAnswerCountInDb(answers.size)
            }
        }
    }

    private fun updateAnswerCountInDb(count: Int) {
        viewModelScope.launch {
            try {
                // Panggil repository untuk update Firestore
                repository.updateAnswerCount(activityId, count)
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Gagal update answer count: ${e.message}")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Timer sekarang menghitung mundur ke server deadline
            while (true) {
                val deadlineTime = _uiState.value.deadline?.time ?: 0L
                val remaining = (deadlineTime - System.currentTimeMillis()) / 1000

                if (remaining <= 0) {
                    _uiState.update { it.copy(remainingTimeInSeconds = 0) }
                    submitQuiz() // Waktu habis, submit!
                    break // Hentikan loop
                }

                _uiState.update { it.copy(remainingTimeInSeconds = remaining) }
                delay(1000L) // Tunggu 1 detik
            }
        }
    }

    private fun findFirstUnanswered(questions: List<QuizQuestion>, answers: Map<String, String>): Int {
        return questions.indexOfFirst { !answers.containsKey(it.id) }.coerceAtLeast(0)
    }

    // --- Aksi dari UI ---

    fun onAnswerSelected(question: QuizQuestion, optionIndex: Int) {
        val optionString = ('A' + optionIndex).toString() // "A", "B", ...
        val isCorrect = (optionString == question.correctAnswer)

        // Simpan ke Firestore
        viewModelScope.launch {
            repository.saveAnswer(
                activityId = activityId,
                questionId = question.id,
                questionNumber = question.questionNumber,
                answerString = optionString,
                isCorrect = isCorrect
            )
        }
        // UI akan update otomatis berkat 'listenForSavedAnswers()'
    }

    fun toggleFlag(questionId: String) {
        val currentFlags = _uiState.value.flaggedQuestions
        _uiState.update {
            it.copy(
                flaggedQuestions = if (currentFlags.contains(questionId)) {
                    currentFlags - questionId
                } else {
                    currentFlags + questionId
                }
            )
        }
    }

    fun selectQuestion(index: Int) {
        _uiState.update { it.copy(currentQuestionIndex = index) }
    }

    fun nextQuestion() {
        val nextIndex = (_uiState.value.currentQuestionIndex + 1).coerceAtMost(_uiState.value.questions.size - 1)
        _uiState.update { it.copy(currentQuestionIndex = nextIndex) }
    }

    fun previousQuestion() {
        val prevIndex = (_uiState.value.currentQuestionIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentQuestionIndex = prevIndex) }
    }

    fun submitQuiz() {
        timerJob?.cancel()

        viewModelScope.launch {
            val questions = _uiState.value.questions
            val answers = _uiState.value.userAnswers

            var correctCount = 0
            questions.forEach { question ->
                if (answers[question.id] == question.correctAnswer) {
                    correctCount++
                }
            }

            // Skor sederhana (misal: 100 / totalSoal * benar)
            val score = if (questions.isNotEmpty()) {
                (100.0 / questions.size * correctCount).toInt()
            } else {
                0
            }

            repository.submitQuiz(
                activityId = activityId,
                score = score,
                correctCount = correctCount,
                answeredCount = answers.size
            )
            // Navigasi kembali akan ditangani oleh Screen
        }
    }

    override fun onCleared() {
        timerJob?.cancel() // Pastikan timer berhenti saat ViewModel hancur
        super.onCleared()
    }
}