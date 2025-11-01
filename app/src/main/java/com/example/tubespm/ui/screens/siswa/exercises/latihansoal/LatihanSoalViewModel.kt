package com.example.tubespm.ui.screens.siswa.exercises.latihansoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.repository.ExerciseCatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LatihanSoalUiState(
    val isLoading: Boolean = true,
    val latihanSoal: List<LatihanSoal> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LatihanSoalViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LatihanSoalUiState())
    val uiState: StateFlow<LatihanSoalUiState> = _uiState.asStateFlow()

    init {
        fetchLatihanSoal()
    }

    private fun fetchLatihanSoal(){
        viewModelScope.launch {
            repository.getLatihanSoal()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(isLoading = false, latihanSoal = list)
                    }
                }
        }
    }
}