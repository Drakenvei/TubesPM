package com.example.tubespm.ui.screens.siswa.homepage

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.repository.ActivityRepository
import com.example.tubespm.repository.ExerciseCatalogRepository
import com.example.tubespm.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChartData(
    val label: String,
    val value: Int,
    val color: Color? = null
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val profilePicture: String = "",
    val userName: String = "User",
    val tryoutRecommendation: List<Tryout> = emptyList(),
    val latestLatihan: List<LatihanSoal> = emptyList(),
    val scoreHistory: List<ChartData> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val catalogRepository: ExerciseCatalogRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // 1. Jalan sendiri: Monitor Profil User (Prioritas Utama)
        observeUserProfile()

        // 2. Jalan sendiri: Monitor Data Dashboard (Tryout, Chart, dll)
        loadDashboardContent()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            // Menggunakan collectLatest agar selalu mendapat update terbaru
            userRepository.getMyProfile()
                .catch { e ->
                    // Jika profil error, jangan hancurkan dashboard lain, cukup log atau set error state kecil
                    _uiState.update { it.copy(error = "Gagal memuat profil: ${e.message}") }
                }
                .collectLatest { user ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            // Update hanya bagian user
                            profilePicture = user.profilePicture,
                            userName = user.name.ifBlank { "Siswa" }
                        )
                    }
                }
        }
    }

    private fun loadDashboardContent() {
        viewModelScope.launch {
            val tryoutFlow = catalogRepository.getTryouts()
            val latihanFlow = catalogRepository.getLatihanSoal()
            val globalActivityFlow = activityRepository.getGlobalRecentActivities(limit = 100)
            val myActivityFlow = activityRepository.getMyTryoutActivities()

            // Combine hanya untuk data konten
            combine(
                tryoutFlow,
                latihanFlow,
                globalActivityFlow,
                myActivityFlow
            ) { tryouts, latihan, globalActivities, myActivities ->

                // --- LOGIKA POPULARITAS & CHART ---
                val popularityMap = globalActivities
                    .groupingBy { it.activityRefId }
                    .eachCount()

                val recommendedTryouts = tryouts.sortedByDescending { tryout ->
                    popularityMap[tryout.id] ?: 0
                }.take(5)

                val completedActivities = myActivities
                    .filter { it.status == "completed" }
                    .sortedBy { it.completedAt }
                    .takeLast(10)

                val charColors = listOf(
                    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF2196F3),
                    Color(0xFF4CAF50), Color(0xFFFF9800)
                )

                val chartData = completedActivities.mapIndexed { index, activity ->
                    ChartData(
                        label = activity.activityTitle.take(10) + "...",
                        value = activity.score,
                        color = charColors[index % charColors.size]
                    )
                }

                // Return data wrapper
                Triple(recommendedTryouts, latihan.take(5), chartData)

            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { (recTryouts, recentLatihan, chartData) ->
                // Update UI State dengan data dashboard
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        tryoutRecommendation = recTryouts,
                        latestLatihan = recentLatihan,
                        scoreHistory = chartData
                    )
                }
            }
        }
    }
}