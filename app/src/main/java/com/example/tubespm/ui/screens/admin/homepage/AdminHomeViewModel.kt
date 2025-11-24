package com.example.tubespm.ui.screens.admin.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminHomeUiState(
    val isLoading: Boolean = true,
    val paketTryoutAktif: Int = 0,
    val soalLatihan: Int = 0,
    val siswaAktif: Int = 0,
    val soalDikerjakan: Int = 0,
    val adminName: String = "Admin"
)

class AdminHomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Simulasi loading data dari API/Firebase
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000) // Simulasi network delay

            _uiState.value = AdminHomeUiState(
                isLoading = false,
                paketTryoutAktif = 25,
                soalLatihan = 2456,
                siswaAktif = 120,
                soalDikerjakan = 8500,
                adminName = "Super Admin" // Bisa diambil dari User Session
            )
        }
    }
}