package com.example.tubespm.ui.screens.siswa.exercises.tryout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.repository.ExerciseCatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// State yang akan diobservasi oleh UI
data class TryoutUiState(
    val isLoading: Boolean = true,
    val tryouts: List<Tryout> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TryoutViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository // Hilt akan menyuntikkan ExerciseCatalogRepositoryImpl
) : ViewModel(){
    private val _uiState = MutableStateFlow(TryoutUiState())
    val uiState: StateFlow<TryoutUiState> = _uiState.asStateFlow()

    init {
        fetchTryouts()
    }

    private fun fetchTryouts() {
        viewModelScope.launch {
            repository.getTryouts()
                .catch { e ->
                    // Tangani error jika gagal
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { tryoutList ->
                    // Update state dengan data baru
                    _uiState.update {
                        it.copy(isLoading = false, tryouts = tryoutList)
                    }
                }
        }
    }
}