package com.example.tubespm.repository

import android.net.Uri
import com.example.tubespm.data.model.UserModel
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /**
     * Mengambil data profil user yang sedang login secara real-time.
     * Menggunakan Flow agar ProfileScreen otomatis update saat data berubah.
     */
    fun getMyProfile(): Flow<UserModel>

    /**
     * Menyimpan perubahan profil (teks dan gambar opsional) ke Firebase.
     */
    suspend fun saveProfile(
        name: String,
//        email: String,
        school: String,
        newImageUri: Uri?,
        currentImageUrl: String
    )
}