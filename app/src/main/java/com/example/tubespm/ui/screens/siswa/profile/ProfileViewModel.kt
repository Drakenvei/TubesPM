package com.example.tubespm.ui.screens.siswa.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null)}

            repository.getMyProfile()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Unknown error") }
                }
                .collect { userModel ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = userModel.name,
                            email = userModel.email,
                            school = userModel.school,
                            tryoutCount = userModel.tryoutCompleted,
                            latihanCount = userModel.latihanCompleted,

                            profileImageUrl = userModel.profilePicture,

                            // ✅ PERBAIKAN: Mapping nilai yang dihitung dari Repository
                            totalPaketDiambil = userModel.totalPaketTaken
                        )
                    }
                }
        }
    }
}