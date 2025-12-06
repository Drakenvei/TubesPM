package com.example.tubespm.data.model

/**
 * Model gabungan untuk UI Pembahasan.
 * Menggabungkan data dari bank soal (Tryout) dan jawaban user (UserActivity).
 */
data class QuestionWithExplanation(
    val id: String,
    val subtest: String,
    val questionText: String,
    val questionImage: String? = null,
    val options: List<String>,
    val optionImages: List<String?> = emptyList(),
    val explanation: String, // Pembahasan
    val explanationImage: String? = null,
    val correctAnswerIndex: Int, // 0='A', 1='B', ...
    val userAnswerIndex: Int? // 0='A', 1='B', ... atau null jika tidak dijawab
)