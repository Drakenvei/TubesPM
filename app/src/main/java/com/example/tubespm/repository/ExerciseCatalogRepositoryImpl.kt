package com.example.tubespm.repository

import com.example.tubespm.data.model.QuizQuestion
import kotlinx.coroutines.tasks.await
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Tryout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseCatalogRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ExerciseCatalogRepository {
    override fun getTryouts(): Flow<List<Tryout>> {
        // Ambil dari koleksi 'tryouts'
        // Filter hanya yang statusnya 'active'
        return db.collection("tryouts")
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots() // Ambil data secara real-time
            .map { snapshot ->
                // Konversi dokumen Firestore ke List<Tryout>
                // Ini berfungsi karena data class Tryout.kt
                // sekarang cocok dengan struktur database
                snapshot.toObjects(Tryout::class.java)
            }
    }

    override fun getLatihanSoal(): Flow<List<LatihanSoal>> {
        // Implementasi serupa untuk latihan soal
        return db.collection("latihan_soal")
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(LatihanSoal::class.java)
            }
    }

    override suspend fun createQuestion(
        parentId: String,
        type: String,
        question: QuizQuestion
    ): String {
        val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"

        // Buat dokumen baru di subcollection questions
        val questionRef = db.collection(collectionName)
            .document(parentId)
            .collection("questions")
            .document()

        // Set data dengan ID yang sudah di-generate
        val questionWithId = question.copy(id = questionRef.id)
        questionRef.set(questionWithId).await()

        return questionRef.id
    }

    override suspend fun updateQuestionCount(parentId: String, type: String, count: Int) {
        val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"
        db.collection(collectionName)
            .document(parentId)
            .update("questionCount", com.google.firebase.firestore.FieldValue.increment(1))
            .await()
    }

    override suspend fun createLatihanSoal(latihan: LatihanSoal): String {
        val docRef = db.collection("latihan_soal").document()
        val latihanWithId = latihan.copy(id = docRef.id)
        docRef.set(latihanWithId).await()
        return docRef.id
    }

    override suspend fun createTryout(tryout: Tryout): String {
        val docRef = db.collection("tryouts").document()
        val tryoutWithId = tryout.copy(id = docRef.id)
        docRef.set(tryoutWithId).await()
        return docRef.id
    }
}