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

    override suspend fun startSubtestSession(
        activityId: String,
        durationInMinutes: Long,
        subtestIndex: Int
    ): Date? {
        val updates = mutableMapOf<String, Any>(
            "status" to "in_progress",
            "currentSubtestIndex" to subtestIndex
        )

        var deadlineTime: Date? = null

        if (durationInMinutes > 0) {
            val now = Calendar.getInstance()
            now.add(Calendar.MINUTE, durationInMinutes.toInt())
            deadlineTime = now.time
            updates["deadline"] = deadlineTime
        } else {
            // Jika latihan soal (durasi 0), hapus deadline atau set null
            updates["deadline"] = FieldValue.delete()
        }

        db.collection("user_activities")
            .document(activityId)
            .update(updates)
            .await()

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
        answeredCount: Int,
        subtestScores: Map<String, Int>
    ) {
        // 1. Ambil dulu dokumen activity untuk tahu userId dan type
        val activityRef = db.collection("user_activities").document(activityId)
        val activitySnapshot = activityRef.get().await()

        val userId = activitySnapshot.getString("userId")?: return
        val type = activitySnapshot.getString("type") ?: "tryout" // "tryout" atau "latihan_soal"
        val currentStatus = activitySnapshot.getString("status")
        val parentId = activitySnapshot.getString("activityRefId") ?: return

        // Cek agar tidak double-count kalau user tekan submit berkali-kali
        if (currentStatus == "completed") return

        // Tentukan koleksi induk (tryouts atau latihan_soal)
        val parentCollection = if (type == "tryout") "tryouts" else "latihan_soal"
        val parentRef = db.collection(parentCollection).document(parentId)
        val userRef = db.collection("users").document(userId)

        // 2. GUNAKAN TRANSACTION
        // Transaction diperlukan agar saat hitung rata-rata, datanya akurat (tidak bentrok dengan user lain yang submit bersamaan)
        db.runTransaction { transaction ->

            // A. Baca Data Parent (Tryout/Latihan) saat ini
            val parentSnapshot = transaction.get(parentRef)

            // Ambil statistik lama (handle null jika ini orang pertama)
            val oldAttemptCount = parentSnapshot.getLong("attemptCount") ?: 0L
            val oldAverage = parentSnapshot.getDouble("averageScore") ?: 0.0
            val oldHighest = parentSnapshot.getDouble("highestScore") ?: 0.0

            // B. Hitung Statistik Baru
            val newAttemptCount = oldAttemptCount + 1

            // Rumus Running Average: ((Rata2 Lama * Jumlah Lama) + Skor Baru) / Jumlah Baru
            val newAverage = ((oldAverage * oldAttemptCount) + score) / newAttemptCount

            // Cek Highest Score
            val newHighest = if (score.toDouble() > oldHighest) score.toDouble() else oldHighest

            // C. Update Dokumen Parent (Update Statistik Global)
            transaction.update(parentRef, mapOf(
                "attemptCount" to newAttemptCount,
                "averageScore" to newAverage,
                "highestScore" to newHighest
            ))

            // D. Update Dokumen User Activity (Tandai Selesai)
            transaction.update(activityRef, mapOf(
                "status" to "completed",
                "score" to score,
                "correctCount" to correctCount,
                "answeredQuestionCount" to answeredCount,
                "subtestScores" to subtestScores,
                "completedAt" to FieldValue.serverTimestamp()
            ))

            // E. Update Counter di User Profile (Opsional, tapi bagus)
            if (type == "tryout") {
                transaction.update(userRef, "tryoutCompleted", FieldValue.increment(1))
            } else {
                transaction.update(userRef, "latihanCompleted", FieldValue.increment(1))
            }

        }.await()
    }
}