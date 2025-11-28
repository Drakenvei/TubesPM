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

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false, // Tambahan flag sukses
    val name: String = "",
    val school: String = "",
    val currentProfileImageUrl: String = "",
    val newSelectedImageUri: Uri? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: UserRepository
    // Context tidak perlu di-inject di sini lagi karena Repository yang butuh context
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

    // Reset status setelah navigasi/error
    fun resetState() {
        _uiState.update { it.copy(error = null, isSaveSuccess = false) }
    }

    fun saveProfile() {
        _uiState.update { it.copy(isSaving = true, error = null) }

        val currentState = _uiState.value

        viewModelScope.launch {
            try {
                // PANGGIL REPOSITORY!
                // Di sinilah logika "Ultra Compression" dijalankan.
                repository.saveProfile(
                    name = currentState.name,
                    school = currentState.school,
                    newImageUri = currentState.newSelectedImageUri,
                    currentImageUrl = currentState.currentProfileImageUrl
                )

                // Jika berhasil:
                _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
            } catch (e: Exception) {
                // Jika gagal (misal file tetap terlalu besar atau koneksi putus)
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Gagal menyimpan") }
            }
        }
    }
}