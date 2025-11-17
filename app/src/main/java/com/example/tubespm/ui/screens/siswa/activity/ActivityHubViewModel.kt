package com.example.tubespm.ui.screens.siswa.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityHubUiState(
    val isLoading: Boolean = true,
    val tryoutCount: Int = 0,
    val latihanCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class ActivityHubViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow(ActivityHubUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchActivityCounts()
    }

    private fun fetchActivityCounts() {
        viewModelScope.launch {
            val tryoutsFlow = repository.getMyTryoutActivities()
            val latihanFlow = repository.getMyLatihanActivities()

            //Gabung kedua flow
            combine(tryoutsFlow, latihanFlow) {tryouts, latihan ->
                // Buat data class sederhana untuk menampung hasil
                Pair(tryouts.size, latihan.size)
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
                .collect { (tryoutCount, latihanCount) ->
                    // Update state dengan jumlah yang sebenarnya
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tryoutCount = tryoutCount,
                            latihanCount = latihanCount
                        )
                    }
                }
        }
    }
}