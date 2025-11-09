package com.example.tubespm.ui.screens.siswa.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// State untuk layar Edit Profile
data class EditProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,

    // Data yang diedit
    val name: String = "",
//    val email: String = "", // Email biasanya tidak boleh diedit, tapi kita ikut UI Anda
    val school: String = "",

    // Untuk gambar
    val currentProfileImageUrl: String = "", // URL gambar dari Firestore
    val newSelectedImageUri: Uri? = null     // URI gambar baru dari galeri
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: UserRepository // Inject repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    // 1. Muat data saat ini untuk mengisi field
    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                // Ambil data dari repository
                // Kita gunakan .first() karena kita hanya butuh data satu kali (bukan real-time)
                val userModel = repository.getMyProfile().first()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = userModel.name,
//                        email = userModel.email,
                        school = userModel.school,
                        currentProfileImageUrl = userModel.profileImageUrl
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }
//    fun onEmailChanged(email: String) {
//        _uiState.update { it.copy(email = email) }
//    }
    fun onSchoolChanged(school: String) {
        _uiState.update { it.copy(school = school) }
    }
    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(newSelectedImageUri = uri) }
    }

    fun saveProfile( onSuccess: () -> Unit){
        val currentState = _uiState.value

        viewModelScope.launch {
            try {
                // Panggil repository untuk menyimpan data
                repository.saveProfile(
                    name = currentState.name,
//                    email = currentState.email,
                    school = currentState.school,
                    newImageUri = currentState.newSelectedImageUri,
                    currentImageUrl = currentState.currentProfileImageUrl
                )
                _uiState.update { it.copy(isSaving = false) }
                onSuccess() // Panggil callback (onBackClick)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.localizedMessage) }
            }
        }
    }



}