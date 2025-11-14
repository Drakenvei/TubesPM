package com.example.tubespm.repository

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

    /**
     * Menambahkan tryout baru ke koleksi user_activities.
     * Ini adalah fungsi "Ambil Tryout".
     */
    suspend fun addTryoutActivity(tryout: Tryout)

    /**
     * Menghapus dokumen dari koleksi user_activities berdasarkan ID uniknya.
     */
    suspend fun cancelTryoutActivity(activityId: String)
}