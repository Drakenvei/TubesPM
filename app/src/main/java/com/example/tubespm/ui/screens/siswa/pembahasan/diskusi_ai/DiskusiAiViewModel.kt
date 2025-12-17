package com.example.tubespm.ui.screens.pembahasan.diskusi_ai

import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.QuestionWithExplanation
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.tubespm.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.Part
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.BlobPart
import com.google.ai.client.generativeai.Chat

// =======================================================
// Data Model & State
// =======================================================

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isPending: Boolean = false
)

data class DiskusiAiUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val questionData: QuestionWithExplanation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false
)

// =======================================================
// ViewModel
// =======================================================

@HiltViewModel
class DiskusiAiViewModel @Inject constructor(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiskusiAiUiState())
    val uiState = _uiState.asStateFlow()

    private val activityId: String? = savedStateHandle.get<String>("activityId")
    private val questionIndex: Int? = savedStateHandle.get<Int>("questionIndex")

    // Urutan subtest yang sama dengan PembahasanViewModel untuk konsistensi sorting
    private val subtestPriority = listOf(
        "pu",    // 1. Penalaran Umum
        "ppu",   // 2. Pengetahuan & Pemahaman Umum
        "pbm",   // 3. Pemahaman Bacaan & Menulis
        "pk",    // 4. Pengetahuan Kuantitatif
        "lbi",   // 5. Literasi Bhs Indonesia
        "lbing", // 6. Literasi Bhs Inggris
        "pm"     // 7. Penalaran Matematika
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var chatSession: Chat? = null

    init {
        loadQuestionData()
    }

    // =======================================================
    // Data Loading Logic
    // =======================================================

    private fun loadQuestionData() {
        if (activityId.isNullOrBlank() || questionIndex == null || questionIndex < 0) {
            _uiState.update { it.copy(isLoading = false, error = "Parameter navigasi tidak valid.") }
            return
        }

        viewModelScope.launch {
            try {
                val activity = repository.getActivity(activityId)
                    ?: throw Exception("Aktivitas tidak ditemukan.")

                // Ambil semua soal asli dari repository
                val allQuestionsRaw = repository.getQuestions(
                    refId = activity.activityRefId,
                    type = activity.type
                )

                // PERBAIKAN: Lakukan sorting yang sama persis dengan PembahasanViewModel
                val sortedQuestions = allQuestionsRaw.sortedWith(
                    compareBy(
                        {
                            val id = it.subtestId.lowercase()
                            val index = subtestPriority.indexOf(id)
                            if (index == -1) Int.MAX_VALUE else index
                        },
                        { it.questionNumber }
                    )
                )

                // Ambil soal berdasarkan index dari list yang SUDAH DIURUTKAN
                val questionToDiscuss = sortedQuestions.getOrNull(questionIndex)
                    ?: throw Exception("Soal ke-${questionIndex + 1} tidak ditemukan.")

                val userAnswers = repository.getSavedAnswers(activityId).first()
                val combinedData = mapData(questionToDiscuss, userAnswers.get(questionToDiscuss.id))

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questionData = combinedData
                    )
                }

                initializeSession(combinedData)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.localizedMessage)
                }
            }
        }
    }

    private fun mapData(
        question: QuizQuestion,
        userAnswerString: String?
    ) : QuestionWithExplanation {
        val correctAnwerIndex = question.correctAnswer.firstOrNull()?.minus('A') ?: 0
        val userAnswerIndex = userAnswerString?.firstOrNull()?.minus('A')

        return QuestionWithExplanation(
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

    private fun base64ToPart(base64String: String?): Part? {
        if (base64String.isNullOrBlank()) return null
        return try {
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            BlobPart("image/jpeg", imageBytes)
        } catch (e: Exception) {
            null
        }
    }

    // =======================================================
    // Chat Logic
    // =======================================================

    private fun initializeSession(questionData: QuestionWithExplanation) {
        if (chatSession != null) return

        val initialParts = mutableListOf<Part>()

        val systemPrompt = """
            Kamu adalah Guru Privat Cerdas untuk aplikasi Tryout UTBK.
            KONTEKS SOAL SAAT INI:
            Mata Pelajaran: ${questionData.subtest}
            Pertanyaan: "${questionData.questionText}"
            Pilihan Jawaban: ${questionData.options.joinToString()}
            Jawaban Benar: Opsi ke-${questionData.correctAnswerIndex + 1}
            Pembahasan Resmi: "${questionData.explanation}"

            Tugasmu adalah membantu pengguna memahami soal dan pembahasan, memberikan petunjuk, atau memberikan penjelasan alternatif.

            ATURAN:
            1. Jangan pernah memberikan jawaban soal secara langsung. Arahkan pengguna untuk memahami pembahasannya.
            2. Pertahankan nada bicara yang profesional, ramah, dan mendidik, seperti seorang guru privat.
            3. Fokus pada konteks soal. Jika pertanyaan tidak relevan, minta pengguna untuk kembali ke topik.
        """.trimIndent()

        initialParts.add(TextPart(systemPrompt))

        val imagePart = base64ToPart(questionData.questionImage)
        if (imagePart != null) {
            initialParts.add(imagePart)
        }

        chatSession = generativeModel.startChat(
            history = listOf(
                Content(
                    role = "user",
                    parts = initialParts
                )
            )
        )

        addMessage(
            ChatMessage(
                "Halo! Saya AI Tutor Anda. Saya siap membantu Anda memahami soal ${questionData.subtest} ini lebih dalam. Apa yang ingin Anda tanyakan?",
                isUser = false
            )
        )
    }

    fun sendMessage(text: String) {
        if (chatSession == null || _uiState.value.isSending || text.isBlank()) return

        _uiState.update { it.copy(isSending = true) }
        addMessage(ChatMessage(text, isUser = true))
        addMessage(ChatMessage("", isUser = false, isPending = true))

        viewModelScope.launch {
            try {
                val response = chatSession?.sendMessage(text)
                removePendingMessage()
                response?.text?.let { aiResponse ->
                    addMessage(ChatMessage(aiResponse, isUser = false))
                }
            } catch (e: Exception) {
                removePendingMessage()
                addMessage(ChatMessage("Maaf, terjadi kesalahan: ${e.localizedMessage}", isUser = false))
            } finally {
                _uiState.update { it.copy(isSending = false) }
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _uiState.update {
            it.copy(messages = it.messages + message)
        }
    }

    private fun removePendingMessage() {
        _uiState.update {
            it.copy(messages = it.messages.filter { msg -> !msg.isPending })
        }
    }
}