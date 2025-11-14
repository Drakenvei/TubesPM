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

data class ActivityUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notStarted: List<ActivityTryoutDetail> = emptyList(),
    val inProgress: List<ActivityTryoutDetail> = emptyList(),
    val completed: List<ActivityTryoutDetail> = emptyList()
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val catalogRepository: ExerciseCatalogRepository //INJECT REPO KATALOG
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchMyActivities()
    }

    private fun fetchMyActivities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. Ambil flow dari aktivitas user (misal: List<UserActivity>)
            val myActivitiesFlow = repository.getMyTryoutActivities()

            // 2. Ambil flow dari semua katalog tryout (misal: List<Tryout>)
            val allTryoutsFlow = catalogRepository.getTryouts()

            // 3. Gabungkan (combine) kedua flow tersebut
            combine(myActivitiesFlow, allTryoutsFlow) { activities, tryouts ->
                // Buat Peta (Map) untuk pencarian cepat
                // Kunci: ID Tryout (misal: "to_utbk_2025_paket1")
                // Nilai: Objek Tryout lengkap
                val tryoutsMap = tryouts.associateBy { it.id }

                // 4. Ubah List<UserActivity> menjadi List<ActivityTryoutDetail>
                activities.mapNotNull { activity ->
                    // Cari metadata tryout di Peta menggunakan ID referensi
                    val tryoutDetail = tryoutsMap[activity.activityRefId]

                    if (tryoutDetail != null) {
                        // Jika ketemu, gabungkan
                        ActivityTryoutDetail(
                            userActivity = activity,
                            tryout = tryoutDetail
                        )
                    } else {
                        // Jika tidak ketemu (misal: admin menghapus tryout), abaikan
                        null
                    }
                }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
                .collect { combinedDetails ->
                    // 5. Pisahkan daftar gabungan ke 3 tab
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