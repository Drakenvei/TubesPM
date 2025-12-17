package com.example.tubespm.ui.screens.admin.homepage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ==========================================
// BAGIAN INI YANG KAMU KURANG TADI
// ==========================================
data class AdminHomeUiState(
    val isLoading: Boolean = true,
    val paketTryoutAktif: Long = 0,
    val soalLatihan: Long = 0,
    val siswaAktif: Long = 0,
    val soalDikerjakan: Long = 0,
    val adminName: String = "Admin",

    // 👇 INI YANG BIKIN ERROR MERAH (Tadi belum ada)
    val adminPhotoBase64: String = "",

    // Chart aktivitas
    val chartFilter: ActivityRange = ActivityRange.WEEKLY,
    val chartPoints: List<ActivityPoint> = emptyList(),
    val chartTotal: Int = 0,
    val isChartLoading: Boolean = true
)

data class ActivityPoint(
    val label: String,
    val value: Int
)

enum class ActivityRange { DAILY, WEEKLY, MONTHLY }

class AdminHomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        observeUserData() // Ambil foto & nama realtime
        loadStatistics()  // Ambil angka statistik
        loadActivityChart(ActivityRange.WEEKLY) // Default tampilan chart
    }

    // Fungsi ambil Foto & Nama (Realtime)
    private fun observeUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    val name = snapshot.getString("name") ?: "Admin"
                    // Ambil string foto
                    val photo = snapshot.getString("profile_picture") ?: ""

                    _uiState.update {
                        it.copy(
                            adminName = name,
                            adminPhotoBase64 = photo // Simpan ke state
                        )
                    }
                }
            }
    }

    // Fungsi Hitung Statistik
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Hitung data pakai Server Aggregate (Cepat)
                // 1. Tryout Aktif (Filter where status == active)
                val tryoutSnap = db.collection("tryouts")
                    .whereEqualTo("status", "active") // [FILTER]
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()

                // 2. Latihan Soal Aktif (Filter where status == active)
                val latihanSnap = db.collection("latihan_soal")
                    .whereEqualTo("status", "active") // [FILTER]
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()

                // 3. Siswa (User role == siswa) -> Opsional, jika mau spesifik
                val siswaSnap = db.collection("users")
                    .whereEqualTo("role", "siswa") // [FILTER] Opsional
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()

                // 4. Aktivitas (Semua)
                val aktivitasSnap = db.collection("user_activities")
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        paketTryoutAktif = tryoutSnap.count,
                        soalLatihan = latihanSnap.count,
                        siswaAktif = siswaSnap.count,
                        soalDikerjakan = aktivitasSnap.count
                    )
                }

            } catch (e: Exception) {
                Log.e("AdminHomeVM", "Error loading stats: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ================= Chart Aktivitas =================
    fun loadActivityChart(range: ActivityRange) {
        viewModelScope.launch {
            val buckets = buildBuckets(range)
            val earliest = buckets.firstOrNull()?.start ?: return@launch

            _uiState.update { it.copy(chartFilter = range, isChartLoading = true) }

            try {
                val snapshot = db.collection("user_activities")
                    .whereGreaterThanOrEqualTo("startedAt", earliest)
                    .get()
                    .await()

                val activities = snapshot.documents.mapNotNull { it.getTimestamp("startedAt")?.toDate() }
                val points = buckets.map { bucket ->
                    val count = activities.count { it.time in bucket.start.time..bucket.end.time }
                    ActivityPoint(bucket.label, count)
                }
                val total = points.sumOf { it.value }

                _uiState.update {
                    it.copy(
                        chartPoints = points,
                        chartTotal = total,
                        isChartLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("AdminHomeVM", "Error load chart: ${e.message}")
                _uiState.update {
                    it.copy(
                        chartPoints = emptyList(),
                        chartTotal = 0,
                        isChartLoading = false
                    )
                }
            }
        }
    }

    private fun buildBuckets(range: ActivityRange): List<DateBucket> {
        return when (range) {
            ActivityRange.DAILY -> buildDailyBuckets()
            ActivityRange.WEEKLY -> buildWeeklyBuckets()
            ActivityRange.MONTHLY -> buildMonthlyBuckets()
        }
    }

    private fun buildDailyBuckets(): List<DateBucket> {
        val cal = Calendar.getInstance()
        cal.setToStartOfDay()

        val result = mutableListOf<DateBucket>()
        // 6 hari ke belakang + hari ini = 7 titik
        for (i in 6 downTo 0) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            dayCal.setToStartOfDay()
            val start = dayCal.time

            val label = dayCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
                ?.uppercase(Locale.getDefault()) ?: "DAY"

            dayCal.add(Calendar.DAY_OF_YEAR, 1)
            val end = dayCal.time

            result.add(DateBucket(label, start, end))
        }
        return result
    }

    private fun buildWeeklyBuckets(): List<DateBucket> {
        val result = mutableListOf<DateBucket>()
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.setToStartOfWeek()

        // 3 minggu lalu sampai minggu ini = 4 titik
        for (i in 3 downTo 0) {
            val weekCal = cal.clone() as Calendar
            weekCal.add(Calendar.WEEK_OF_YEAR, -i)
            weekCal.setToStartOfWeek()
            val start = weekCal.time
            val weekNumber = weekCal.get(Calendar.WEEK_OF_YEAR)
            val label = "W$weekNumber"

            weekCal.add(Calendar.DAY_OF_YEAR, 7)
            val end = weekCal.time

            result.add(DateBucket(label, start, end))
        }
        return result
    }

    private fun buildMonthlyBuckets(): List<DateBucket> {
        val result = mutableListOf<DateBucket>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.setToStartOfDay()

        // 5 bulan lalu sampai bulan ini = 6 titik
        for (i in 5 downTo 0) {
            val monthCal = cal.clone() as Calendar
            monthCal.add(Calendar.MONTH, -i)
            monthCal.set(Calendar.DAY_OF_MONTH, 1)
            monthCal.setToStartOfDay()
            val start = monthCal.time

            monthCal.add(Calendar.MONTH, 1)
            val end = monthCal.time

            val label = SimpleDateFormat("MMM", Locale.getDefault()).format(start).uppercase(Locale.getDefault())
            result.add(DateBucket(label, start, end))
        }
        return result
    }
}

private data class DateBucket(
    val label: String,
    val start: Date,
    val end: Date
)

private fun Calendar.setToStartOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

private fun Calendar.setToStartOfWeek() {
    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    setToStartOfDay()
}