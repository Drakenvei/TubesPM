package com.example.tubespm.ui.screens.admin.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.utils.ImageUtils // Pastikan file utilitas Base64 Anda ada di sini
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val profilePictureBase64: String = "", // Field untuk gambar Base64
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
            // Dengarkan perubahan data profil secara realtime
            db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _uiState.update { it.copy(error = e.message) }
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val name = snapshot.getString("name") ?: "Admin"
                        val email = snapshot.getString("email") ?: currentUser.email ?: ""
                        val profilePic = snapshot.getString("profile_picture") ?: "" // Ambil field gambar

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
                // Hitung User (kecuali admin jika perlu, tapi ini count total sederhana)
                val usersSnapshot = db.collection("users").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
                val userCount = usersSnapshot.count

                // Hitung Tryout
                val tryoutSnapshot = db.collection("tryouts").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
                val tryoutCount = tryoutSnapshot.count

                // Hitung Latihan
                val latihanSnapshot = db.collection("latihan_soal").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await()
                val exerciseCount = latihanSnapshot.count

                _uiState.update {
                    it.copy(
                        userCount = userCount.toString(),
                        tryoutCount = tryoutCount.toString(),
                        exerciseCount = exerciseCount.toString()
                    )
                }
            } catch (e: Exception) {
                // Ignore error stats for now
            }
        }
    }

    /**
     * Fungsi Update Foto Profil
     * Menerima URI, konversi ke Base64, lalu update Firestore.
     */
    fun updateProfilePicture(context: Context, uri: Uri) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Konversi URI ke Base64
            val base64Image = ImageUtils.uriToBase64(context, uri)

            if (base64Image != null) {
                // 2. Update Firestore
                try {
                    db.collection("users").document(currentUser.uid)
                        .update("profile_picture", base64Image)
                        .await()

                    Toast.makeText(context, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show()
                    // UI otomatis update karena ada addSnapshotListener di loadProfileData
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal update foto: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}