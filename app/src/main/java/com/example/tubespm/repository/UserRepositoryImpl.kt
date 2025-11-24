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

    // --- GET PROFILE (REAL-TIME) ---
    override fun getMyProfile(): Flow<UserModel> = callbackFlow {
        val uid = currentUid
        if (uid == null) {
            trySend(UserModel())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toUserModel(uid)
                    trySend(user)
                } else {
                    trySend(UserModel(uid = uid, email = auth.currentUser?.email ?: ""))
                }
            }
        awaitClose { listener.remove() }
    }

    // --- SAVE PROFILE (ULTRA COMPRESSION) ---
    override suspend fun saveProfile(
        name: String,
        school: String,
        newImageUri: Uri?,
        currentImageUrl: String
    ) {
        val uid = currentUid ?: throw Exception("User tidak login")

        try {
            val base64Image = if (newImageUri != null) {
                // Gunakan logika kompresi baru yang lebih hemat memori
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

    /**
     * ULTRA COMPRESSION:
     * 1. Cek ukuran tanpa load ke memori (Mencegah Crash).
     * 2. Load versi kecil (Subsampling).
     * 3. Resize paksa ke 500px.
     * 4. Kompres sampai di bawah 200KB.
     */
    private suspend fun uriToBase64Ultra(uri: Uri): String = withContext(Dispatchers.IO) {
        // TAHAP 1: Cek Dimensi Awal (Tanpa load pixel ke RAM)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true // Hanya baca lebar & tinggi
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        // TAHAP 2: Hitung Faktor Pengecilan (inSampleSize)
        // Target resolusi baca awal: mendekati 500px
        var inSampleSize = 1
        val reqSize = 500
        while ((options.outHeight / inSampleSize) > reqSize || (options.outWidth / inSampleSize) > reqSize) {
            inSampleSize *= 2
        }

        // TAHAP 3: Load Gambar dengan Skala Kecil (Hemat Memori)
        val scaledOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = inSampleSize
        }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, scaledOptions)
        } ?: throw Exception("Gagal membaca file gambar")

        // TAHAP 4: Pastikan Resize ke Maksimal 500px (Extra Safety)
        // Kadang inSampleSize masih menyisakan gambar 600-700px, kita paksa ke 500px
        val maxDimension = 500
        val ratio = maxDimension.toDouble() / maxOf(bitmap.width, bitmap.height)
        val finalBitmap = if (ratio < 1.0) {
            val newW = (bitmap.width * ratio).toInt()
            val newH = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else {
            bitmap
        }

        // TAHAP 5: Kompresi JPEG Iteratif (Target Biner < 200KB)
        // 200KB Binary = ~266KB Base64. SANGAT AMAN untuk Firestore (Limit 1MB).
        var quality = 100
        var stream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        while (stream.toByteArray().size > 200_000 && quality > 10) {
            stream = ByteArrayOutputStream() // Reset
            quality -= 15 // Kurangi kualitas drastis (15%) tiap putaran
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            Log.d("Upload", "Kompres ulang: Quality=$quality, Size=${stream.size()} bytes")
        }

        val bytes = stream.toByteArray()
        Log.d("Upload", "Final Base64 Size: ${bytes.size} bytes")

        // Encode ke Base64
        Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}