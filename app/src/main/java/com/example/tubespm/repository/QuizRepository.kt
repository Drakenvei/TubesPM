package com.example.tubespm.repository

import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.UserActivity
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface QuizRepository {

    /** Mengambil 1 dokumen UserActivity untuk tahu tryout apa yang dikerjakan */
    suspend fun getActivity(activityId: String): UserActivity?

    /** Mengambil 1 dokumen Tryout untuk tahu metadata (durasi, dll) */
    suspend fun getTryoutMetadata(tryoutId: String): Tryout?

    /** Mengambil SEMUA soal untuk tryoutId tertentu */
    suspend fun getTryoutQuestions(tryoutId: String): List<QuizQuestion>

    /** Mengambil jawaban yang sudah disimpan user (Map<QuestionID, PilihanUser>) */
    fun getSavedAnswers(activityId: String): Flow<Map<String, String>>

    /** Menyimpan satu jawaban user ke sub-koleksi 'answers' */
    suspend fun saveAnswer(
        activityId: String,
        questionId: String,
        questionNumber: Int,
        answerString: String, // "A", "B", "C", ...
        isCorrect: Boolean
    )

    /** Mengupdate status aktivitas (misal: "in_progress" atau "completed") */
    suspend fun updateActivityStatus(activityId: String, status: String)

    /**
     * Meng-update status ke 'in_progress' DAN mengatur deadline di Firestore.
     * Mengembalikan deadline yang telah di-commit.
     */
    suspend fun startQuizSession(activityId: String, durationInMinutes: Long): Date

    /** Meng-update jumlah soal terjawab di Firestore */
    suspend fun updateAnswerCount(activityId: String, count: Int)

    /** Menyimpan skor akhir ke dokumen UserActivity */
    suspend fun submitQuiz(
        activityId: String,
        score: Int,
        correctCount: Int,
        answeredCount: Int
    )
}