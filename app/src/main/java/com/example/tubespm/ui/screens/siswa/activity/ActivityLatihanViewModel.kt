package com.example.tubespm.ui.screens.siswa.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.repository.ActivityRepository
import com.example.tubespm.repository.ExerciseCatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityLatihanUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notStarted: List<ActivityLatihanDetail> = emptyList(),
    val inProgress: List<ActivityLatihanDetail> = emptyList(),
    val completed: List<ActivityLatihanDetail> = emptyList()
)

@HiltViewModel
class ActivityLatihanViewModel @Inject constructor(
    private val repository: ActivityRepository,
//    private val catalogRepository: ExerciseCatalogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityLatihanUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchMyLatihanActivities()
    }

    private fun fetchMyLatihanActivities(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Ambil flow user activities
            repository.getMyLatihanActivities()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
                .collect { activities ->
                    // 2. Ambil detail LatihanSoal secara parallel (Async)
                    // Menggunakan getLatihanSoalById agar status Inactive/Deleted tetap terambil
                    val details = activities.map { activity ->
                        async {
                            val latihan = repository.getLatihanSoalById(activity.activityRefId)
                            if (latihan != null) {
                                ActivityLatihanDetail(activity, latihan)
                            } else {
                                null // Hard deleted from DB
                            }
                        }
                    }.awaitAll().filterNotNull()

                    // 3. Update State
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notStarted = details.filter { d -> d.userActivity.status == "not_started" }
                                .sortedByDescending { d -> d.userActivity.startedAt },
                            inProgress = details.filter { d -> d.userActivity.status == "in_progress" }
                                .sortedByDescending { d -> d.userActivity.startedAt },
                            completed = details.filter { d -> d.userActivity.status == "completed" }
                                .sortedByDescending { d -> d.userActivity.completedAt }
                        )
                    }
                }
        }
    }

    fun cancelLatihan(activityId: String){
        viewModelScope.launch {
            try {
                repository.cancelLatihanActivity(activityId)
                // Flow akan otomatis update UI
            } catch (e: Exception){
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }
}