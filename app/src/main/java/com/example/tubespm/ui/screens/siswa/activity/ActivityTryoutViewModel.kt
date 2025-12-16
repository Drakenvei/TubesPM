package com.example.tubespm.ui.screens.siswa.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.UserActivity
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

data class ActivityTryoutUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notStarted: List<ActivityTryoutDetail> = emptyList(),
    val inProgress: List<ActivityTryoutDetail> = emptyList(),
    val completed: List<ActivityTryoutDetail> = emptyList()
)

@HiltViewModel
class ActivityTryoutViewModel @Inject constructor(
    private val repository: ActivityRepository,
//    private val catalogRepository: ExerciseCatalogRepository //INJECT REPO KATALOG
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityTryoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchMyActivities()
    }

    private fun fetchMyActivities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Ambil Flow Aktivitas Siswa
            repository.getMyTryoutActivities()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
                .collect { activities ->
                    // 2. Untuk setiap aktivitas, ambil Detail Tryout-nya secara async
                    //    Kita gunakan 'map' + 'async' untuk fetch parallel agar cepat
                    val details = activities.map { activity ->
                        async {
                            // [PENTING] Ambil Tryout berdasarkan ID, tanpa peduli status (Active/Inactive/Deleted)
                            // Fungsi ini ada di Repository (lihat langkah 1)
                            val tryout = repository.getTryoutById(activity.activityRefId)

                            if (tryout != null) {
                                ActivityTryoutDetail(activity, tryout)
                            } else {
                                // Jika return null, berarti Hard Delete (benar-benar hilang dari DB)
                                null
                            }
                        }
                    }.awaitAll().filterNotNull() // Tunggu semua selesai, filter yang null

                    // 3. Update State & Pisahkan Tab
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            notStarted = details.filter { it.userActivity.status == "not_started" },
                            inProgress = details.filter { it.userActivity.status == "in_progress" },
                            completed = details.filter { it.userActivity.status == "completed" }
                        )
                    }
                }

        }
    }

    // --- TAMBAHKAN FUNGSI INI UNTUK TOMBOL "BATALKAN" ---
    fun cancelTryout(activityId: String) {
        viewModelScope.launch {
            try {
                repository.cancelTryoutActivity(activityId)
                // Tidak perlu update state manual, Flow akan otomatis
                // mengirimkan daftar baru tanpa item yang dihapus.
            } catch (e: Exception) {
                // Tampilkan error jika gagal menghapus
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }
}