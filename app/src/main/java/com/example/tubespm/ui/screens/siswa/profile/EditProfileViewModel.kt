package com.example.tubespm.ui.screens.siswa.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.repository.UserRepository
import com.example.tubespm.utils.ImageUtils
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val name: String = "",
    val school: String = "",
    val currentProfileImageUrl: String = "", // Berisi Base64 atau URL lama
    val newSelectedImageUri: Uri? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    @ApplicationContext private val context: Context // Inject Context untuk ImageUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val userModel = repository.getMyProfile().first()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = userModel.name,
                        school = userModel.school,
                        // Ambil dari field 'profilePicture' yang baru diupdate di UserModel
                        // (Pastikan UserModel.kt Anda sudah menggunakan profilePicture bukan profileImageUrl)
                        currentProfileImageUrl = userModel.profilePicture
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun onNameChanged(name: String) = _uiState.update { it.copy(name = name) }
    fun onSchoolChanged(school: String) = _uiState.update { it.copy(school = school) }
    fun onImageSelected(uri: Uri) = _uiState.update { it.copy(newSelectedImageUri = uri) }

    fun saveProfile(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isSaving = true) }

        val currentState = _uiState.value
        val userId = Firebase.auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, Any>(
                    "name" to currentState.name,
                    "school" to currentState.school
                )

                // Jika ada gambar baru dipilih, konversi ke Base64
                if (currentState.newSelectedImageUri != null) {
                    val base64Image = ImageUtils.uriToBase64(context, currentState.newSelectedImageUri)
                    if (base64Image != null) {
                        updates["profile_picture"] = base64Image // Simpan ke field 'profile_picture'
                    }
                }

                // Update Firestore langsung (atau panggil repository jika sudah diupdate)
                Firebase.firestore.collection("users").document(userId)
                    .update(updates)
                    .await()

                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.localizedMessage) }
            }
        }
    }
}