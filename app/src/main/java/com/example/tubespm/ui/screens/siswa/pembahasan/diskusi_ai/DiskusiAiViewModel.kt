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
// ** IMPORTS BASE CLIENT 0.6.0 YANG SUDAH TERVERIFIKASI **
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

    // FIX: Ambil parameter sebagai nullable untuk menghindari crash jika null
    private val activityId: String? = savedStateHandle.get<String>("activityId")
    private val questionIndex: Int? = savedStateHandle.get<Int>("questionIndex")

    // API Key disembunyikan untuk keamanan, asumsikan ini sudah benar
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var chatSession: Chat? = null

    // --- Inisialisasi ---
    init {
        loadQuestionData()
    }

    // =======================================================
    // Data Loading Logic
    // =======================================================

    private fun loadQuestionData() {
        // Validasi yang sama, namun kini memeriksa null secara eksplisit
        if (activityId.isNullOrBlank() || questionIndex == null || questionIndex < 0) {
            // Error ini akan muncul jika PembahasanScreen mengirimkan ID kosong atau null, atau index < 0
            _uiState.update { it.copy(isLoading = false, error = "Parameter navigasi tidak valid.") }
            return
        }

        // Ambil nilai yang sudah divalidasi
        val id = activityId
        val index = questionIndex

        viewModelScope.launch {
            try {
                val activity = repository.getActivity(id)
                    ?: throw Exception("Aktivitas tidak ditemukan.")
                val allQuestions = repository.getQuestions(
                    refId = activity.activityRefId,
                    type = activity.type
                )

                val questionToDiscuss = allQuestions.getOrNull(index)
                    ?: throw Exception("Soal ke-${index + 1} tidak ditemukan.")
                val userAnswers = repository.getSavedAnswers(id).first()

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
        // Konversi "A" -> 0, "B" -> 1, dst.
        val correctAnwerIndex = question.correctAnswer.firstOrNull()?.minus('A') ?: 0
        val userAnswerIndex = userAnswerString?.firstOrNull()?.minus('A') // Bisa null

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
            println("Error decoding Base64 image: ${e.message}")
            null
        }
    }

    // =======================================================
    // Chat Logic (Menggunakan Constructor KTX/Base Client)
    // =======================================================

    private fun initializeSession(questionData: QuestionWithExplanation) {
        if (chatSession != null) return

        val initialParts = mutableListOf<Part>()

        // --- SYSTEM PROMPT ---
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

        // --- Image Context ---
        val imagePart = base64ToPart(questionData.questionImage)
        if (imagePart != null) {
            initialParts.add(imagePart)
        }

        // Mulai sesi chat dengan system prompt dan image (jika ada)
        chatSession = generativeModel.startChat(
            history = listOf(
                Content(
                    role = "user",
                    parts = initialParts
                )
            )
        )

        // Tambahkan pesan sambutan
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
        val userMessage = ChatMessage(text, isUser = true)
        addMessage(userMessage)
        val pendingMessage = ChatMessage("", isUser = false, isPending = true)
        addMessage(pendingMessage)

        viewModelScope.launch {
            try {
                removePendingMessage()
                val response = chatSession?.sendMessage(text)
                response?.text?.let { aiResponse ->
                    addMessage(ChatMessage(aiResponse, isUser = false))
                }
            } catch (e: Exception) {
                removePendingMessage()
                addMessage(ChatMessage("Maaf, terjadi kesalahan saat menghubungi AI. Coba lagi: ${e.localizedMessage}", isUser = false))
            } finally {
                _uiState.update { it.copy(isSending = false) }
            }
        }
    }

    // [ Helper functions addMessage, removePendingMessage, onCleared tetap sama ]
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

    override fun onCleared() {
        // Hapus chatSession jika diperlukan, namun untuk Gemini 2.5, tidak ada metode close() eksplisit.
        super.onCleared()
    }
}