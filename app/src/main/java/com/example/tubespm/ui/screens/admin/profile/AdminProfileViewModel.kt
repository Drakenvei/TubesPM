package com.example.tubespm.ui.screens.admin.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class AdminProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val profilePictureBase64: String = "",
    val userCount: String = "0",
    val tryoutCount: String = "0",
    val exerciseCount: String = "0",
    val error: String? = null
)

class AdminProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        loadProfileData()
        loadStatistics()
    }

    private fun loadProfileData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _uiState.update { it.copy(error = e.message) }
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val name = snapshot.getString("name") ?: "Admin"
                        val email = snapshot.getString("email") ?: currentUser.email ?: ""
                        val profilePic = snapshot.getString("profile_picture") ?: ""

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                name = name,
                                email = email,
                                profilePictureBase64 = profilePic
                            )
                        }
                    }
                }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                // Statistik sederhana (menggunakan count aggregation)
                val usersSnapshot = db.collection("users").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
                val tryoutSnapshot = db.collection("tryouts").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
                val latihanSnapshot = db.collection("latihan_soal").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()

                _uiState.update {
                    it.copy(
                        userCount = usersSnapshot.count.toString(),
                        tryoutCount = tryoutSnapshot.count.toString(),
                        exerciseCount = latihanSnapshot.count.toString()
                    )
                }
            } catch (e: Exception) {
                // Ignore stat errors
            }
        }
    }

    /**
     * Update Foto Profil dengan ULTRA COMPRESSION
     * Agar foto besar (5MB+) muat di Firestore (Limit 1MB)
     */
    fun updateProfilePicture(context: Context, uri: Uri) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. Proses gambar dengan Kompresi Agresif (Background Thread)
                val base64Image = processImageToBase64(context, uri)

                // 2. Update Firestore
                db.collection("users").document(currentUser.uid)
                    .update("profile_picture", base64Image)
                    .await()

                Toast.makeText(context, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // --- LOGIKA KOMPRESI "ULTRA" (Copy dari UserRepositoryImpl siswa) ---
    private suspend fun processImageToBase64(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        // A. Cek Dimensi Awal (Tanpa Load Memori)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // B. Hitung Skala Pengecilan (Target 500px)
        var inSampleSize = 1
        val reqSize = 500
        while ((options.outHeight / inSampleSize) > reqSize || (options.outWidth / inSampleSize) > reqSize) {
            inSampleSize *= 2
        }

        // C. Load Gambar Skala Kecil
        val scaledOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = inSampleSize
        }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, scaledOptions)
        } ?: throw Exception("Gagal membaca gambar")

        // D. Resize Paksa ke Maksimal 500px
        val maxDimension = 500
        val ratio = maxDimension.toDouble() / maxOf(bitmap.width, bitmap.height)
        val finalBitmap = if (ratio < 1.0) {
            val newW = (bitmap.width * ratio).toInt()
            val newH = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else {
            bitmap
        }

        // E. Kompresi Iteratif (Target < 200KB Binary)
        var quality = 100
        var stream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        while (stream.toByteArray().size > 200_000 && quality > 10) {
            stream = ByteArrayOutputStream()
            quality -= 15
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        }

        val bytes = stream.toByteArray()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}