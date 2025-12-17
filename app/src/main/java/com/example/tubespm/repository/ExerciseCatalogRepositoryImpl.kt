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

    // Nama koleksi
    private val tryoutsCollection = db.collection("tryouts")
    private val latihanSoalCollection = db.collection("latihan_soal") // <--- TAMBAH INI

    override fun getTryouts(): Flow<List<Tryout>> {
        // Ambil dari koleksi 'tryouts'
        // Filter hanya yang statusnya 'active'
        return tryoutsCollection
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots() // Ambil data secara real-time
            .map { snapshot ->
                // Konversi dokumen Firestore ke List<Tryout>
                snapshot.toObjects(Tryout::class.java)
            }
    }

    override fun getLatihanSoal(): Flow<List<LatihanSoal>> {
        // Implementasi serupa untuk latihan soal
        return latihanSoalCollection // <--- Ganti dengan variabel koleksi
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
        val docRef = latihanSoalCollection.document() // <--- Ganti dengan variabel koleksi
        // SAAT MEMBUAT, KITA PASTIKAN FIELD LOWERCASE TERISI
        val latihanWithId = latihan.copy(
            id = docRef.id,
            codeLower = latihan.code.trim().lowercase(), // <--- TAMBAH INI
            titleLower = latihan.title.trim().lowercase() // <--- TAMBAH INI
        )
        docRef.set(latihanWithId).await()
        return docRef.id
    }

    override suspend fun createTryout(tryout: Tryout): String {
        val docRef = tryoutsCollection.document()
        // Saat membuat, kita pastikan field lowercase terisi
        val tryoutWithId = tryout.copy(
            id = docRef.id,
            codeLower = tryout.code.trim().lowercase(), // Simpan versi lowercase
            titleLower = tryout.title.trim().lowercase() // Simpan versi lowercase
        )
        docRef.set(tryoutWithId).await()
        return docRef.id
    }

    // =========================================================
    // IMPLEMENTASI FUNGSI BARU UNTUK VALIDASI DUPLIKASI
    // =========================================================

    /**
     * Memeriksa apakah Tryout dengan kode tertentu sudah ada di database.
     * Pengecekan dilakukan secara case-insensitive menggunakan field 'codeLower'.
     */
    override suspend fun isTryoutCodeDuplicate(code: String): Boolean {
        return try {
            val lowerCaseCode = code.trim().lowercase() // Konversi input ke lowercase
            val snapshot = tryoutsCollection
                .whereEqualTo("codeLower", lowerCaseCode) // Query ke field lowercase
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            println("Error checking tryout code duplication: ${e.message}")
            false
        }
    }

    /**
     * Memeriksa apakah Tryout dengan judul tertentu sudah ada di database.
     * Pengecekan dilakukan secara case-insensitive menggunakan field 'titleLower'.
     */
    override suspend fun isTryoutTitleDuplicate(title: String): Boolean {
        return try {
            val lowerCaseTitle = title.trim().lowercase() // Konversi input ke lowercase
            val snapshot = tryoutsCollection
                .whereEqualTo("titleLower", lowerCaseTitle) // Query ke field lowercase
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            println("Error checking tryout title duplication: ${e.message}")
            false
        }
    }

    // --- IMPLEMENTASI FUNGSI BARU UNTUK LATIHAN SOAL ---

    /**
     * Memeriksa apakah Latihan Soal dengan kode tertentu sudah ada di database.
     */
    override suspend fun isLatihanSoalCodeDuplicate(code: String): Boolean {
        return try {
            val lowerCaseCode = code.trim().lowercase()
            val snapshot = latihanSoalCollection // <--- PENTING: Gunakan koleksi Latihan Soal
                .whereEqualTo("codeLower", lowerCaseCode)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            println("Error checking latihan soal code duplication: ${e.message}")
            false
        }
    }

    /**
     * Memeriksa apakah Latihan Soal dengan judul tertentu sudah ada di database.
     */
    override suspend fun isLatihanSoalTitleDuplicate(title: String): Boolean {
        return try {
            val lowerCaseTitle = title.trim().lowercase()
            val snapshot = latihanSoalCollection // <--- PENTING: Gunakan koleksi Latihan Soal
                .whereEqualTo("titleLower", lowerCaseTitle)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            println("Error checking latihan soal title duplication: ${e.message}")
            false
        }
    }
}