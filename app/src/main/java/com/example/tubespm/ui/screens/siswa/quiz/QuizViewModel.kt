package com.example.tubespm.ui.screens.siswa.quiz

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

data class QuizUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Data per subtest
    val activeQuestions: List<QuizQuestion> = emptyList(),
    val subtestName: String = "",
    val currentSubtestIndex: Int = 0,
    val totalSubtests: Int = 0,
    val isLastSubtest: Boolean = false,

    // Data Global
    val userAnswers: Map<String, String> = emptyMap(), // Map<QuestionID, AnswerString>
    val flaggedQuestions: Set<String> = emptySet(), // Set<QuestionID>
    val currentQuestionIndex: Int = 0, // Index relatif terhadap activeQuestions (0..20)
    val remainingTimeInSeconds: Long = 0L,
    val quizMode: QuizMode = QuizMode.TRYOUT,
    val deadline: Date? = null
)

// Helper class untuk meratakan struktur
data class FlattenedSubtest(
    val sectionName: String,
    val subtestName: String,
    val subtestId: String,
    val duration: Int,
    val questionCount: Int
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

    // Simpan semua data mentah di sini (bukan di UI State)
    private var allQuestionRaw: List<QuizQuestion> = emptyList()
    private var allSubtestsFlat: List<FlattenedSubtest> = emptyList()

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
                // Ambil data aktivitas
                val activity = repository.getActivity(activityId) ?: throw Exception("Aktivitas null")

                // Tentukan Mode berdasarkan 'type' dari database
                val mode = if (activity.type == "tryout") QuizMode.TRYOUT else QuizMode.LATIHAN

                // Ambil Metadata Tryout & Ratakan Struktur Subtest
                val tryoutMetadata = repository.getQuizMetadata(activity.activityRefId, activity.type)
                    ?: throw Exception("Data soal null")

                // Ambil Soal DULUAN agar kita tahu jumlahnya untuk mode Latihan
                allQuestionRaw = repository.getQuestions(activity.activityRefId, activity.type)

                // Logika Pembuatan Subtest List
                if (mode == QuizMode.TRYOUT) {
                    // Ratakan struktur Section -> Subtest menjadi List<FlattenedSubtest>
                    allSubtestsFlat = tryoutMetadata.sections.flatMap { section ->
                        section.subtests.map { subtest ->
                            FlattenedSubtest(
                                sectionName = section.sectionName,
                                subtestName = subtest.subtestName,
                                subtestId = subtest.subtestId,
                                duration = subtest.duration, // Ambil durasi per subtest
                                questionCount = subtest.questionCount
                            )
                        }
                    }
                } else {
                    // Logika Latihan: Buat 1 Subtest Dummy yang berisi SEMUA soal
                    // Karena Latihan Soal di database tidak punya array 'sections'
                    allSubtestsFlat = listOf(
                        FlattenedSubtest(
                            sectionName = "Latihan",
                            subtestName = tryoutMetadata.title,
                            subtestId = "ALL_LATIHAN", // ID Dummy
                            duration = 0, // Tidak ada durasi
                            questionCount = allQuestionRaw.size
                        )
                    )
                }

                /// Validasi agar tidak crash lagi
                if (allSubtestsFlat.isEmpty()) {
                    throw Exception("Struktur soal kosong. Cek database.")
                }

                // Tentukan Subtest Mana Yang Aktif
                // Ambil dari database (jika user resume) atau mulai dari 0
                val targetIndex = activity.currentSubtestIndex.coerceIn(0, allSubtestsFlat.lastIndex)

                // Muat subtest tersebut
                loadSubtest(targetIndex, activity.deadline, mode)

                // Listen Jawaban
                listenForSavedAnswers()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                Log.e("QuizViewModel", "Error loading quiz: ${e.message}")
            }
        }
    }

    // Logika Memuat Subtest Spesifik
    private fun loadSubtest(index: Int, existingDeadline: Date?, mode: QuizMode) {
        val currentSubtest = allSubtestsFlat[index]

        // Filter soal: Hanya ambil soal yang subtestId-nya cocok
        val subtestQuestions = if (mode == QuizMode.TRYOUT) {
            allQuestionRaw.filter { it.subtestId == currentSubtest.subtestId }
        } else {
            // Latihan: Ambil SEMUA soal (karena cuma 1 sesi)
            allQuestionRaw
        }

        viewModelScope.launch {
            var deadline = existingDeadline
            var remaining = 0L

            // Atur timer
            if (mode == QuizMode.TRYOUT) {
                // Jika deadline null (baru masuk subtest ini), set deadline baru
                if (existingDeadline == null) {
                    deadline = repository.startSubtestSession(
                        activityId,
                        currentSubtest.duration.toLong(), // Durasi subtest ini
                        index
                    )
                }

                if (deadline != null) {
                    remaining = (deadline.time - System.currentTimeMillis()) / 1000
                }
            } else {
                // Mode latihan, update index saja tanpa timer
                repository.startSubtestSession(activityId, 0, index)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeQuestions = subtestQuestions, //Hanya soal subtest ini
                    subtestName = currentSubtest.subtestName,
                    currentSubtestIndex = index,
                    totalSubtests = allSubtestsFlat.size,
                    isLastSubtest = index == allSubtestsFlat.lastIndex,
                    currentQuestionIndex = 0, // Reset ke soal no 1
                    remainingTimeInSeconds = remaining,
                    quizMode = mode,
                    deadline = deadline
                )
            }

            if (mode == QuizMode.TRYOUT) startTimer()
        }
    }

    // Pindah ke Subtest Berikutnya (Dipanggil tombol "Lanjut Subtest" atau Waktu Habis)

    fun finishCurrentSubtest(){
        timerJob?.cancel()
        val currentIndex = _uiState.value.currentSubtestIndex

        if (currentIndex < allSubtestsFlat.lastIndex) {
            // Masih ada subtest berikutnya -> Load Next (Deadline null agar dibuat baru)
            loadSubtest(currentIndex + 1, null, _uiState.value.quizMode)
        } else {
            // Sudah subtest terakhir -> Submit Semua
            submitQuiz()
        }
    }

    private fun listenForSavedAnswers() {
        viewModelScope.launch {
            repository.getSavedAnswers(activityId)
                .distinctUntilChanged() // <-- Hanya jika ada jawaban BARU
                .collect { answers ->
                    _uiState.update { it.copy( userAnswers = answers) }
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
                    // Waktu habis = Lanjut Subtest / Submit
                    finishCurrentSubtest()
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
        val nextIndex = (_uiState.value.currentQuestionIndex + 1)
            .coerceAtMost(_uiState.value.activeQuestions.size - 1)
        _uiState.update { it.copy(currentQuestionIndex = nextIndex) }
    }

    fun previousQuestion() {
        val prevIndex = (_uiState.value.currentQuestionIndex - 1)
            .coerceAtLeast(0)
        _uiState.update { it.copy(currentQuestionIndex = prevIndex) }
    }

    fun submitQuiz() {
        timerJob?.cancel()

        viewModelScope.launch {
            val questions = allQuestionRaw
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

            withContext(NonCancellable) {
                repository.submitQuiz(
                    activityId = activityId,
                    score = score,
                    correctCount = correctCount,
                    answeredCount = answers.size
                )
            }

            // Navigasi kembali akan ditangani oleh Screen
        }
    }

    override fun onCleared() {
        timerJob?.cancel() // Pastikan timer berhenti saat ViewModel hancur
        super.onCleared()
    }
}