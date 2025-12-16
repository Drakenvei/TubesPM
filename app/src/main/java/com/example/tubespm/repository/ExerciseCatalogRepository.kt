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

    suspend fun createLatihanSoal(latihan: LatihanSoal): String

    suspend fun createTryout(tryout: Tryout): String

    // --- FUNGSI BARU UNTUK VALIDASI DUPLIKASI ---
    /**
     * Memeriksa apakah Tryout dengan kode tertentu sudah ada di database.
     */
    suspend fun isTryoutCodeDuplicate(code: String): Boolean

    /**
     * Memeriksa apakah Tryout dengan judul tertentu sudah ada di database.
     */
    suspend fun isTryoutTitleDuplicate(title: String): Boolean


}