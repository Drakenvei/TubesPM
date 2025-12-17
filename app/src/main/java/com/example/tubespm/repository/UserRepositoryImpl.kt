package com.example.tubespm.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.tubespm.data.model.UserModel
import com.example.tubespm.data.model.toUserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : UserRepository {

    private val currentUid: String?
        get() = auth.currentUser?.uid

    // --- GET PROFILE (REAL-TIME DENGAN PENGGABUNGAN DATA) ---
    override fun getMyProfile(): Flow<UserModel> = callbackFlow {
        val uid = currentUid
        if (uid == null) {
            trySend(UserModel())
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val userDocRef = db.collection("users").document(uid)

        val listener = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val baseUserModel = snapshot.toUserModel(uid)

                launch {
                    try {
                        // ✅ PERUBAHAN: Hanya menghitung Tryout yang diambil
                        val totalTryoutTaken = getTotalTryoutPackagesTaken(uid)

                        // Mengirim UserModel yang lengkap dengan nilai baru
                        // totalPaketTaken sekarang HANYA berisi Tryout
                        trySend(baseUserModel.copy(totalPaketTaken = totalTryoutTaken)).isSuccess
                    } catch (e: Exception) {
                        Log.e("UserRepository", "Gagal menghitung total paket tryout: ${e.message}")
                        trySend(baseUserModel.copy(totalPaketTaken = 0)).isSuccess
                    }
                }
            } else {
                trySend(UserModel(uid = uid, email = auth.currentUser?.email ?: "")).isSuccess
            }
        }

        awaitClose { listener.remove() }
    }

    // ✅ FUNGSI BARU: Menghitung Total Paket Tryout yang Diambil dari /user_activities
    private suspend fun getTotalTryoutPackagesTaken(uid: String): Int = withContext(Dispatchers.IO) {
        // Query Firestore untuk menghitung dokumen 'tryout' yang dimiliki user ini
        return@withContext try {
            val snapshot = db.collection("user_activities")
                .whereEqualTo("userId", uid)
                // ✅ FILTER KUNCI: Hanya yang bertipe "tryout"
                .whereEqualTo("type", "tryout")
                .get()
                .await()

            snapshot.size() // Mengambil jumlah dokumen tryout
        } catch (e: Exception) {
            Log.e("UserRepository", "Gagal query user_activities (Tryout): ${e.message}", e)
            0
        }
    }

    // --- SAVE PROFILE (ULTRA COMPRESSION) ---
    override suspend fun saveProfile(
        name: String,
        school: String,
        newImageUri: Uri?,
        currentImageUrl: String
    ) {
        // ... (Kode tidak berubah) ...
        val uid = currentUid ?: throw Exception("User tidak login")

        try {
            val base64Image = if (newImageUri != null) {
                uriToBase64Ultra(newImageUri)
            } else {
                currentImageUrl
            }

            val userData = mapOf(
                "name" to name,
                "school" to school,
                "profile_picture" to base64Image
            )

            db.collection("users").document(uid).update(userData).await()

        } catch (e: Exception) {
            Log.e("UserRepository", "Gagal menyimpan profil: ${e.message}", e)
            throw e
        }
    }

    private suspend fun uriToBase64Ultra(uri: Uri): String = withContext(Dispatchers.IO) {
        // ... (Kode kompresi tidak berubah) ...
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        var inSampleSize = 1
        val reqSize = 500
        while ((options.outHeight / inSampleSize) > reqSize || (options.outWidth / inSampleSize) > reqSize) {
            inSampleSize *= 2
        }

        val scaledOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = inSampleSize
        }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, scaledOptions)
        } ?: throw Exception("Gagal membaca file gambar")

        val maxDimension = 500
        val ratio = maxDimension.toDouble() / maxOf(bitmap.width, bitmap.height)
        val finalBitmap = if (ratio < 1.0) {
            val newW = (bitmap.width * ratio).toInt()
            val newH = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else {
            bitmap
        }

        var quality = 100
        var stream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        while (stream.toByteArray().size > 200_000 && quality > 10) {
            stream = ByteArrayOutputStream()
            quality -= 15
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            Log.d("Upload", "Kompres ulang: Quality=$quality, Size=${stream.size()} bytes")
        }

        val bytes = stream.toByteArray()
        Log.d("Upload", "Final Base64 Size: ${bytes.size} bytes")

        Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}