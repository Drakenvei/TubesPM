package com.example.tubespm.repository

import android.net.Uri
import com.example.tubespm.data.model.UserModel
import com.example.tubespm.data.model.toUserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {
    private val currentUid: String?
        get() = auth.currentUser?.uid

    override fun getMyProfile(): Flow<UserModel> {
        val uid = currentUid
        if (uid == null){
            // Kembalikan user kosong jika tidak login
            return flowOf(UserModel())
        }

        // snapshots() akan otomatis mengirim data baru setiap ada perubahan di Firestore
        return db.collection("users").document(uid)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toUserModel(uid) // Konversi snapshot ke data model
                } else {
                    // Buat user baru jika belum ada datanya
                    UserModel(uid = uid, email = auth.currentUser?.email ?: "")
                }
            }
    }

    override suspend fun saveProfile(
        name: String,
//        email: String, //email biasanya tidak diubah
        school: String,
        newImageUri: Uri?,
        currentImageUrl: String
    ) {
        val uid = currentUid ?: throw Exception("User tidak login")

        // 1. Tentukan URL gambar yang akan disimpan
        val imageUrlToSave = if (newImageUri != null) {
            // Jika ada gambar baru, upload dulu dan dapatkan URL-nya
            uploadImage(uid, newImageUri)
        } else {
            // Jika tidak, pakai URL yang lama
            currentImageUrl
        }

        // 2. Buat map data untuk di-update ke Firestore
        val userData = mapOf(
            "name" to name,
//            "email" to email,
            "school" to school,
            "profileImageUrl" to imageUrlToSave
        )

        // 3. Update dokumen di Firestore
        db.collection("users").document(uid).update(userData).await()
    }

    /**
     * Helper function untuk upload gambar ke Firebase Storage
     */
    private suspend fun uploadImage(uid: String, uri: Uri): String{
        // Buat path unik: "profile_images/{USER_ID}/image_name.jpg"
        val imageRef = storage.reference.child("profile_images/$uid/${uri.lastPathSegment}")

        // Upload file
        val uploadTask = imageRef.putFile(uri).await()

        // Dapatkan download URL
        return uploadTask.storage.downloadUrl.await().toString()
    }
}