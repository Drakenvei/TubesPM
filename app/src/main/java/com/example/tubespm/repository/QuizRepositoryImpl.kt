package com.example.tubespm.data.repository

import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.UserActivity
import com.example.tubespm.repository.QuizRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : QuizRepository {

    override suspend fun getActivity(activityId: String): UserActivity? {
        return db.collection("user_activities").document(activityId)
            .get().await().toObject(UserActivity::class.java)
    }

    override suspend fun getTryoutMetadata(tryoutId: String): Tryout? {
        return db.collection("tryouts").document(tryoutId)
            .get().await().toObject(Tryout::class.java)
    }

    override suspend fun getTryoutQuestions(tryoutId: String): List<QuizQuestion> {
        return db.collection("tryouts").document(tryoutId)
            .collection("questions")
            .orderBy("questionNumber") // Pastikan soal terurut
            .get().await().toObjects(QuizQuestion::class.java)
    }

    override fun getSavedAnswers(activityId: String): Flow<Map<String, String>> {
        // Menggunakan snapshots() agar UI update real-time saat jawaban disimpan
        return db.collection("user_activities").document(activityId)
            .collection("answers")
            .snapshots()
            .map { snapshot ->
                // Ubah list dokumen jawaban menjadi Map<QuestionID, AnswerString>
                snapshot.documents.associate { doc ->
                    doc.id to (doc.getString("userAnswer") ?: "")
                }.filter { it.value.isNotEmpty() }
            }
    }

    override suspend fun saveAnswer(
        activityId: String,
        questionId: String,
        questionNumber: Int,
        answerString: String,
        isCorrect: Boolean
    ) {
        val answerData = mapOf(
            "questionNumber" to questionNumber,
            "userAnswer" to answerString,
            "isCorrect" to isCorrect
        )
        // Set dokumen dengan ID soal, menimpa jawaban lama jika ada
        db.collection("user_activities").document(activityId)
            .collection("answers").document(questionId)
            .set(answerData).await()
    }

    override suspend fun updateActivityStatus(activityId: String, status: String) {
        db.collection("user_activities").document(activityId)
            .update("status", status).await()
    }

    override suspend fun startQuizSession(activityId: String, durationInMinutes: Long): Date {
        // Hitung deadline
        val now = Calendar.getInstance()
        now.add(Calendar.MINUTE, durationInMinutes.toInt())
        val deadlineTime = now.time // Ini adalah objek Date

        // Update status dan deadline di Firestore
        db.collection("user_activities").document(activityId)
            .update(
                "status", "in_progress",
                "deadline", deadlineTime // Simpan timestamp deadline
            ).await()

        return deadlineTime // Kembalikan deadline
    }

    override suspend fun updateAnswerCount(activityId: String, count: Int) {
        db.collection("user_activities").document(activityId)
            .update("answeredQuestionCount", count).await()
    }

    override suspend fun submitQuiz(
        activityId: String,
        score: Int,
        correctCount: Int,
        answeredCount: Int
    ) {
        val finalData = mapOf(
            "status" to "completed",
            "score" to score,
            "correctCount" to correctCount,
            "answeredQuestionCount" to answeredCount, // <-- Update hitungan
            "completedAt" to FieldValue.serverTimestamp()
        )
        db.collection("user_activities").document(activityId)
            .update(finalData).await()
    }
}