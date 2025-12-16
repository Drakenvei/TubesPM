package com.example.tubespm.repository

import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.UserActivity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {

    /**
     * Mengambil SEMUA aktivitas tryout milik user yang sedang login.
     * Menggunakan Flow agar data real-time.
     */
    fun getMyTryoutActivities(): Flow<List<UserActivity>>

    /**
     * Mengambil SEMUA aktivitas LATIHAN SOAL milik user yang sedang login.
     */
    fun getMyLatihanActivities(): Flow<List<UserActivity>>

//
//    Mengambil aktivitas global 30 hari terakhir
//
    fun getGlobalRecentActivities(limit: Long = 100): Flow<List<UserActivity>>

    // [BARU] Tambahkan ini agar ViewModel bisa mengambil detail Tryout
    // meskipun statusnya inactive/deleted (untuk ditampilkan di riwayat siswa)
    suspend fun getTryoutById(tryoutId: String): Tryout?

    // [BARU] Tambahkan ini untuk Latihan Soal
    suspend fun getLatihanSoalById(latihanId: String): LatihanSoal?

    /**
     * Menambahkan tryout baru ke koleksi user_activities.
     * Ini adalah fungsi "Ambil Tryout".
     */
    suspend fun addTryoutActivity(tryout: Tryout)

    /**
     * Menambahkan latihan soal baru ke koleksi user_activities.
     * Ini adalah fungsi "Ambil Latihan Soal".
     */
    suspend fun addLatihanActivity(latihan: LatihanSoal)

    /**
     * Menghapus dokumen dari koleksi user_activities berdasarkan ID uniknya.
     */
    suspend fun cancelTryoutActivity(activityId: String)

    suspend fun cancelLatihanActivity(activityId: String)
}