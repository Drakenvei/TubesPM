package com.example.tubespm.ui.screens.siswa.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.repository.ActivityRepository
import com.example.tubespm.repository.ExerciseCatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val catalogRepository: ExerciseCatalogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityLatihanUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchMyLatihanActivities()
    }

    private fun fetchMyLatihanActivities(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Ambil flow aktivitas latihan (List<UserActivity>)
            val myActivitiesFlow = repository.getMyLatihanActivities()

            // 2. Ambil flow katalog latihan (List<LatihanSoal>)
            val allLatihanFlow = catalogRepository.getLatihanSoal()

            // 3. Gabungkan semuanya
            combine(myActivitiesFlow, allLatihanFlow) { activities, latihanList ->

                val latihanMap = latihanList.associateBy {it.id} // Map untuk pencarian cepat

                // 4. Ubah List<UserActivity> menjadi List<ActivityLatihanDetail>
                activities.mapNotNull { activity ->
                    val latihanDetail = latihanMap[activity.activityRefId]

                    if (latihanDetail != null) {
                        ActivityLatihanDetail(
                            userActivity = activity,
                            latihanSoal = latihanDetail
                        )
                    } else {
                        null // Abaikan jika katalognya tidak ditemukan
                    }
                }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
                .collect { combinedDetails ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notStarted = combinedDetails.filter { d -> d.userActivity.status == "not_started" },
                            inProgress = combinedDetails.filter { d -> d.userActivity.status == "in_progress" },
                            completed = combinedDetails.filter { d -> d.userActivity.status == "completed" }
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