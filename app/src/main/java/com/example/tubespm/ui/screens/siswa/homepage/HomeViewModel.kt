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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    private val  _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData(){
        viewModelScope.launch {
            // Ambil user profile
            val userFlow = userRepository.getMyProfile()

            // Ambil Katalog Tryout (untuk rekomendasi)
            val tryoutFlow = catalogRepository.getTryouts()

            // Ambil katalog latihan
            val latihanFlow = catalogRepository.getLatihanSoal()

            // Ambil Aktivitas GLOBAL (30 hari terakhir) untuk hitung popularitas
            // ambil sampel 100 aktivitas terakhir untuk menentukan tren
            val globalActivityFlow = activityRepository.getGlobalRecentActivities(limit = 100)

            // Ambil Aktivitas Pribadi (untuk chart statistik)
            val myActivityFlow = activityRepository.getMyTryoutActivities()

            // Gabungkan semua flow
            combine(
                userFlow,
                tryoutFlow,
                latihanFlow,
                globalActivityFlow,
                myActivityFlow
            ) { user, tryouts, latihan, globalActivities, myActivities ->

                // --- LOGIKA POPULARITAS ---
                // Hitung frekuensi setiap tryoutId di globalActivities
                // Map<TryoutID, JumlahDiambil>
                val popularityMap = globalActivities
                    .groupingBy { it.activityRefId }
                    .eachCount()

                // Urutkan Tryout berdasarkan jumlah diambil (Descending)
                val recommendedTryouts = tryouts.sortedByDescending { tryout ->
                    popularityMap[tryout.id] ?: 0 // Jika tidak ada di map, anggap 0
                }.take(5) // Ambil 5 teratas

                // Proses data Chart (hanya ambil yang selesai)
                val completedActivities = myActivities
                    .filter { it.status == "completed" }
                    .sortedBy { it.completedAt } // Urutkan berdasarkan tanggal selesai
                    .takeLast(10) // ambil 10 terakhir

                // Warna untuk chart looping
                val charColors = listOf(
                    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF2196F3),
                    Color(0xFF4CAF50), Color(0xFFFF9800)
                )

                val chartData = completedActivities.mapIndexed { index, activity ->
                    ChartData(
                        label = activity.activityTitle.take(10) + "...", // Potong judul jika kepanjangan
                        value = activity.score,
                        color = charColors[index % charColors.size]
                    )
                }

                HomeUiState(
                    isLoading = false,
                    profilePicture = user.profilePicture,
                    userName = user.name.ifBlank { "Siswa" },
                    tryoutRecommendation = recommendedTryouts,
                    latestLatihan = latihan.take(5),
                    scoreHistory = chartData,
                    error = null
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}