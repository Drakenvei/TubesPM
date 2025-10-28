package com.example.tubespm.repository

import android.util.Log
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.data.model.QuestionWithExplanation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class QuestionRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /**
     * RELATIONSHIP EXPLANATION:
     *
     * Each question has either:
     * - tryoutId + sectionId (for tryout questions)
     * - latihanId (for latihan questions)
     *
     * Example structure:
     * questions/
     *   └── question1/
     *       ├── id: 1
     *       ├── tryoutId: "1" (or null if latihan)
     *       ├── sectionId: "TPS" (optional, for tryout sections)
     *       ├── latihanId: "1" (or null if tryout)
     *       ├── type: "tryout" | "latihan"
     *       └── ...
     */

    // Fetch questions for a specific tryout section
    suspend fun getQuestionsByTryoutSection(
        tryoutId: Int,
        sectionTitle: String
    ): List<QuizQuestion> {
        return try {
            val snapshot = db.collection("questions")
                .whereEqualTo("tryoutId", tryoutId.toString())
                .whereEqualTo("sectionId", sectionTitle) // e.g., "TPS", "Literasi"
                .whereEqualTo("type", "tryout")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    QuizQuestion(
                        id = (doc.getLong("id") ?: 0).toInt(),
                        subtest = doc.getString("subtest") ?: "",
                        questionText = doc.getString("questionText") ?: "",
                        options = (doc.get("options") as? List<String>) ?: emptyList(),
                        correctAnswerIndex = (doc.getLong("correctAnswerIndex") ?: 0).toInt()
                    )
                } catch (e: Exception) {
                    Log.e("QuestionRepository", "Error parsing question: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Error fetching tryout questions: ${e.message}")
            emptyList()
        }
    }

    // Fetch ALL questions for a tryout (across all sections)
    suspend fun getQuestionsByTryoutId(tryoutId: Int): List<QuizQuestion> {
        return try {
            val snapshot = db.collection("questions")
                .whereEqualTo("tryoutId", tryoutId.toString())
                .whereEqualTo("type", "tryout")
                .orderBy("sectionId", Query.Direction.ASCENDING)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    QuizQuestion(
                        id = (doc.getLong("id") ?: 0).toInt(),
                        subtest = doc.getString("subtest") ?: "",
                        questionText = doc.getString("questionText") ?: "",
                        options = (doc.get("options") as? List<String>) ?: emptyList(),
                        correctAnswerIndex = (doc.getLong("correctAnswerIndex") ?: 0).toInt()
                    )
                } catch (e: Exception) {
                    Log.e("QuestionRepository", "Error parsing question: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Error fetching tryout questions: ${e.message}")
            emptyList()
        }
    }

    // Fetch questions for a specific latihan soal package
    suspend fun getQuestionsByLatihanId(latihanId: Int): List<QuizQuestion> {
        return try {
            val snapshot = db.collection("questions")
                .whereEqualTo("latihanId", latihanId.toString())
                .whereEqualTo("type", "latihan")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    QuizQuestion(
                        id = (doc.getLong("id") ?: 0).toInt(),
                        subtest = doc.getString("subtest") ?: "",
                        questionText = doc.getString("questionText") ?: "",
                        options = (doc.get("options") as? List<String>) ?: emptyList(),
                        correctAnswerIndex = (doc.getLong("correctAnswerIndex") ?: 0).toInt()
                    )
                } catch (e: Exception) {
                    Log.e("QuestionRepository", "Error parsing question: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Error fetching latihan questions: ${e.message}")
            emptyList()
        }
    }

    // Fetch questions with explanations (for pembahasan)
    suspend fun getQuestionsWithExplanation(
        id: Int,
        type: String,
        userAnswers: Map<Int, Int>, // Map of questionId to selectedAnswerIndex
        sectionId: String? = null // Optional, for tryout sections
    ): List<QuestionWithExplanation> {
        return try {
            val idField = if (type == "tryout") "tryoutId" else "latihanId"

            var query = db.collection("questions")
                .whereEqualTo(idField, id.toString())
                .whereEqualTo("type", type)

            // Add section filter if provided (for tryout sections)
            if (sectionId != null && type == "tryout") {
                query = query.whereEqualTo("sectionId", sectionId)
            }

            val snapshot = query
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val questionId = (doc.getLong("id") ?: 0).toInt()
                    QuestionWithExplanation(
                        id = questionId,
                        subtest = doc.getString("subtest") ?: "",
                        questionText = doc.getString("questionText") ?: "",
                        options = (doc.get("options") as? List<String>) ?: emptyList(),
                        correctAnswerIndex = (doc.getLong("correctAnswerIndex") ?: 0).toInt(),
                        userAnswerIndex = userAnswers[questionId],
                        explanation = doc.getString("explanation") ?: "Pembahasan tidak tersedia"
                    )
                } catch (e: Exception) {
                    Log.e("QuestionRepository", "Error parsing question with explanation: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Error fetching questions with explanation: ${e.message}")
            emptyList()
        }
    }

    // Get question count for validation
    suspend fun getQuestionCount(id: Int, type: String, sectionId: String? = null): Int {
        return try {
            val idField = if (type == "tryout") "tryoutId" else "latihanId"

            var query = db.collection("questions")
                .whereEqualTo(idField, id.toString())
                .whereEqualTo("type", type)

            if (sectionId != null && type == "tryout") {
                query = query.whereEqualTo("sectionId", sectionId)
            }

            val snapshot = query.get().await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Error counting questions: ${e.message}")
            0
        }
    }
}