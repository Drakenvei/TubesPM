package com.example.tubespm.ui.screens.admin.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val userCount: String = "0",
    val tryoutCount: String = "0",
    val exerciseCount: String = "0"
)

class AdminProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(800) // Simulasi fetch data

            _uiState.value = AdminProfileUiState(
                isLoading = false,
                name = "Administrator",
                email = "admin.utama@tubespm.com",
                userCount = "128",
                tryoutCount = "12",
                exerciseCount = "45"
            )
        }
    }
}