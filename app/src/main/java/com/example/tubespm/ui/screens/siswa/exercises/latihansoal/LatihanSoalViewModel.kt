package com.example.tubespm.ui.screens.siswa.exercises.latihansoal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.repository.ActivityRepository
import com.example.tubespm.repository.ExerciseCatalogRepository
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LatihanSoalCatalogItem(
    val latihanSoal: LatihanSoal,
    val isTaken: Boolean // true jika sudah ada di 'user_activities'
)

data class LatihanSoalUiState(
    val isLoading: Boolean = true,
    val latihanSoal: List<LatihanSoalCatalogItem> = emptyList(),
    val error: String? = null
)

sealed class LatihanSoalEvent {
    data class ShowToast(val message: String) : LatihanSoalEvent()
}

@HiltViewModel
class LatihanSoalViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LatihanSoalUiState())
    val uiState: StateFlow<LatihanSoalUiState> = _uiState.asStateFlow()

    // 1. StateFlow untuk menyimpan query pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- CHANNEL EVENT ---
    private val _events = Channel<LatihanSoalEvent>()
    val events = _events.receiveAsFlow()

    // --- STATE UNTUK DIALOG KONFIRMASI ---
    private val _showConfirmationDialog = MutableStateFlow<LatihanSoal?>(null)
    val showConfirmationDialog = _showConfirmationDialog.asStateFlow()

    init {
        observeFilteredLatihanSoal()
    }

    // 2. Fungsi publik untuk UI (SearchBar) untuk memperbarui query
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // 3. Fungsi ini sekarang menggabungkan data DENGAN query pencarian
    private fun observeFilteredLatihanSoal(){
        viewModelScope.launch {
            // Ambil flow dari katalog latihan (List<LatihanSoal>)
            val latihanSoalFlow = repository.getLatihanSoal()

            //Ambil flow dari aktivitas user (List<UserActivity>)
            // dan ubah menjadi Set<String> berisi ID yang sudah diambil
            val takenLatihanSoalIdsFlow = activityRepository.getMyLatihanActivities()
                .map { activities ->
                    activities.map { it.activityRefId }.toSet()
                }

            // Gabungkan (combine) TIGA flow
            combine(latihanSoalFlow, takenLatihanSoalIdsFlow, _searchQuery) { latihanSoalList, takenIds, query ->

                val sortedList = latihanSoalList.sortedByDescending { it.createdAt }

                //Map data mentah ke LatihanSoalCatalogItem
                val catalogItems = sortedList.map { latihan ->
                    LatihanSoalCatalogItem(
                        latihanSoal = latihan,
                        isTaken = takenIds.contains(latihan.id) // Cek duplikat
                    )
                }

                //Filter berdasarkan query
                if (query.isBlank()) {
                    // Jika query kosong, tampilkan semua latihansoal
                    catalogItems
                } else {
                    // Jika query ada, filter daftar berdasarkan judul
                    catalogItems.filter { item ->
                        val titelMatch = item.latihanSoal.title.contains(query, ignoreCase = true)
                        val codeMatch = item.latihanSoal.code.contains(query, ignoreCase = true)
                        titelMatch || codeMatch
                    }
                }
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
                .collect { filteredItems ->
                    _uiState.update {
                        // Update UI state dengan daftar yang sudah difilter
                        it.copy(isLoading = false, latihanSoal = filteredItems)
                    }
                }
        }
    }

    // --- FUNGSI UNTUK MEMICU DIALOG ---
    fun onTakeLatihanSoalClicked(latihan: LatihanSoal) {
        _showConfirmationDialog.value = latihan
    }

    fun onDismissDialog() {
        _showConfirmationDialog.value = null
    }

    fun confirmTakeLatihan() {
        val latihan = _showConfirmationDialog.value ?: return
        _showConfirmationDialog.value = null // Tutup dialog segera

        viewModelScope.launch {
            try {
                activityRepository.addLatihanActivity(latihan)
                // Kirim Sinyal Toast Sukses
                _events.send(LatihanSoalEvent.ShowToast("Latihan Soal berhasil diambil! Cek menu Activity."))
            } catch (e: Exception) {
                Log.e("LatihanSoalViewModel", "Gagal mengambil latihan: ${e.message}")
                _events.send(LatihanSoalEvent.ShowToast("Gagal mengambil Latihan Soal."))
            }
        }
    }
}