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

    override suspend fun getQuizMetadata(refId: String, type: String): Tryout? {
        val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"

        // Kita convert ke object Tryout.
        // Pastikan model LatihanSoal dan Tryout punya field yang mirip
        // atau gunakan model Tryout sebagai wadah umum sementara.
        return db.collection(collectionName).document(refId)
            .get().await().toObject(Tryout::class.java)
    }

    override suspend fun getQuestions(refId: String, type: String): List<QuizQuestion> {
        val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"

        return db.collection(collectionName).document(refId)
            .collection("questions")
            .orderBy("questionNumber")
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

    override suspend fun startQuizSession(activityId: String, durationInMinutes: Long): Date? {
        val updates = mutableMapOf<String, Any>("status" to "in_progress")
        var deadlineTime: Date? = null

        // Hanya set deadline jika durasi > 0 (yaitu Tryout)
        if (durationInMinutes > 0) {
            val now = Calendar.getInstance()
            now.add(Calendar.MINUTE, durationInMinutes.toInt())
            deadlineTime = now.time
            updates["deadline"] = deadlineTime
        }

        db.collection("user_activities").document(activityId)
            .update(updates).await()

        return deadlineTime
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
        // 1. Ambil dulu dokumen activity untuk tahu userId dan type
        val activityRef = db.collection("user_activities").document(activityId)
        val activitySnapshot = activityRef.get().await()

        val userId = activitySnapshot.getString("userId")
        val type = activitySnapshot.getString("type") // "tryout" atau "latihan_soal"
        val currentStatus = activitySnapshot.getString("status")

        // Cek agar tidak double-count kalau user tekan submit berkali-kali
        if (currentStatus == "completed") return

        // 2. Siapkan update untuk user_activities
        val activityUpdates = mapOf(
            "status" to "completed",
            "score" to score,
            "correctCount" to correctCount,
            "answeredQuestionCount" to answeredCount, // <-- Update hitungan
            "completedAt" to FieldValue.serverTimestamp()
        )

        // 3. Jalankan Batch Write (Atomik) agar aman
        db.runBatch { batch ->
            // A. Update status di user_activities
            batch.update(activityRef, activityUpdates)

            // B. Update counter di users (Hanya jika userId valid)
            if (userId != null) {
                val userRef = db.collection("users").document(userId)

                if (type == "tryout"){
                    // Increment tryoutCompleted + 1
                    batch.update(userRef, "tryoutCompleted", FieldValue.increment(1))
                } else {
                    // Increment latihanCompleted + 1 (asumsi tipe lain adalah latihan)
                    batch.update(userRef, "latihanCompleted", FieldValue.increment(1))
                }
            }
        }.await()
    }
}