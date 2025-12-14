package com.example.tubespm.ui.screens.admin.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.AggregateSource
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
    val userCount: String = "0", // Default 0
    val tryoutCount: String = "0",
    val exerciseCount: String = "0",
    val error: String? = null,
    // BARU: State untuk fitur ganti password
    val passwordChangeSuccess: Boolean = false,
    val passwordChangeError: String? = null
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
                    if (e != null) { return@addSnapshotListener }
                    if (snapshot != null && snapshot.exists()) {
                        val name = snapshot.getString("name") ?: "Admin"
                        val email = snapshot.getString("email") ?: currentUser.email ?: ""
                        val profilePic = snapshot.getString("profile_picture") ?: ""

                        _uiState.update {
                            it.copy(isLoading = false, name = name, email = email, profilePictureBase64 = profilePic)
                        }
                    }
                }
        }
    }

    // --- FUNGSI STATISTIK ---
    private fun loadStatistics() {
        viewModelScope.launch {
            Log.d("AdminStat", "Mulai menghitung data...")

            try {
                // 1. Hitung User (Siswa Saja): Memfilter koleksi 'users' di mana 'role' adalah 'siswa'
                val usersTask = db.collection("users")
                    .whereEqualTo("role", "siswa")
                    .count()
                    .get(AggregateSource.SERVER)

                // 2. Hitung Tryout: Koleksi 'tryouts'
                val tryoutTask = db.collection("tryouts")
                    .count()
                    .get(AggregateSource.SERVER)

                // 3. Hitung Latihan: Koleksi 'latihan_soal'
                val latihanTask = db.collection("latihan_soal")
                    .count()
                    .get(AggregateSource.SERVER)

                // Tunggu hasil (Async)
                val usersSnapshot = usersTask.await()
                val tryoutSnapshot = tryoutTask.await()
                val latihanSnapshot = latihanTask.await()

                Log.d("AdminStat", "Hasil: User=${usersSnapshot.count}, Tryout=${tryoutSnapshot.count}, Latihan=${latihanSnapshot.count}")

                _uiState.update {
                    it.copy(
                        userCount = usersSnapshot.count.toString(),
                        tryoutCount = tryoutSnapshot.count.toString(),
                        exerciseCount = latihanSnapshot.count.toString()
                    )
                }

            } catch (e: Exception) {
                Log.e("AdminStat", "GAGAL Menghitung: ${e.message}")
                e.printStackTrace()

                _uiState.update { it.copy(userCount = "Err", tryoutCount = "Err", exerciseCount = "Err") }
            }
        }
    }

    // --- FUNGSI BARU UNTUK GANTI PASSWORD ---
    fun changePassword(context: Context, newPassword: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "Admin belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            _uiState.update { it.copy(passwordChangeError = "Password minimal 6 karakter.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, passwordChangeSuccess = false, passwordChangeError = null) }
            try {
                // Memanggil fungsi updatePassword dari Firebase Auth
                user.updatePassword(newPassword).await()

                // Berhasil
                _uiState.update { it.copy(isLoading = false, passwordChangeSuccess = true) }
                Toast.makeText(context, "Password berhasil diperbarui!", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Log.e("AdminPass", "GAGAL ganti password: ${e.message}")
                val errorMessage = if (e.message?.contains("auth/requires-recent-login") == true) {
                    "Admin harus login ulang untuk mengganti password."
                } else {
                    "Gagal ganti password: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                }
                _uiState.update { it.copy(isLoading = false, passwordChangeError = errorMessage) }
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Fungsi untuk membersihkan state error/success
    fun clearError() {
        _uiState.update { it.copy(passwordChangeSuccess = false, passwordChangeError = null) }
    }


    // --- FUNGSI PROFILE PICTURE ---
    fun updateProfilePicture(context: Context, uri: Uri) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val base64Image = processImageToBase64(context, uri)
                db.collection("users").document(currentUser.uid).update("profile_picture", base64Image).await()
                Toast.makeText(context, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun processImageToBase64(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        var inSampleSize = 1
        val reqSize = 500
        while ((options.outHeight / inSampleSize) > reqSize || (options.outWidth / inSampleSize) > reqSize) { inSampleSize *= 2 }
        val scaledOptions = BitmapFactory.Options().apply { inJustDecodeBounds = false; inSampleSize = inSampleSize }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, scaledOptions) } ?: throw Exception("Gagal membaca gambar")
        val maxDimension = 500
        val ratio = maxDimension.toDouble() / maxOf(bitmap.width, bitmap.height)
        val finalBitmap = if (ratio < 1.0) { Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true) } else { bitmap }
        var quality = 100
        var stream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        while (stream.toByteArray().size > 200_000 && quality > 10) { stream = ByteArrayOutputStream(); quality -= 15; finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream) }
        Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }
}