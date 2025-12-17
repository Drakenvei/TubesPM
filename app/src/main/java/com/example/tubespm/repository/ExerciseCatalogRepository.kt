package com.example.tubespm.repository

import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.data.model.Tryout
import kotlinx.coroutines.flow.Flow

// Interface ini mendefinisikan "apa" yang bisa dilakukan repository
interface ExerciseCatalogRepository {
    fun getTryouts(): Flow<List<Tryout>>
    fun getLatihanSoal(): Flow<List<LatihanSoal>>

    suspend fun createQuestion(
        parentId: String,
        type: String, // "tryout" atau "latihan_soal"
        question: QuizQuestion
    ): String

    suspend fun updateQuestionCount(parentId: String, type: String, count: Int)

    suspend fun createLatihanSoal(latihan: LatihanSoal): String // <--- Akan diperbarui implementasinya

    suspend fun createTryout(tryout: Tryout): String

    // --- FUNGSI BARU UNTUK VALIDASI DUPLIKASI TRYOUT ---
    suspend fun isTryoutCodeDuplicate(code: String): Boolean
    suspend fun isTryoutTitleDuplicate(title: String): Boolean

    // --- FUNGSI BARU UNTUK VALIDASI DUPLIKASI LATIHAN SOAL ---
    /**
     * Memeriksa apakah Latihan Soal dengan kode tertentu sudah ada di database.
     */
    suspend fun isLatihanSoalCodeDuplicate(code: String): Boolean // <--- TAMBAH INI

    /**
     * Memeriksa apakah Latihan Soal dengan judul tertentu sudah ada di database.
     */
    suspend fun isLatihanSoalTitleDuplicate(title: String): Boolean // <--- TAMBAH INI
}