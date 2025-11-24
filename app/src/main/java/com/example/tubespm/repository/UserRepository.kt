package com.example.tubespm.repository

import android.net.Uri
import com.example.tubespm.data.model.UserModel
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // Mengambil data profil secara real-time (Stream)
    fun getMyProfile(): Flow<UserModel>

    // Menyimpan data. newImageUri bisa null jika user tidak ganti foto.
    // currentImageUrl di sini sebenarnya akan berisi string Base64 lama.
    suspend fun saveProfile(
        name: String,
        school: String,
        newImageUri: Uri?,
        currentImageUrl: String
    )
}