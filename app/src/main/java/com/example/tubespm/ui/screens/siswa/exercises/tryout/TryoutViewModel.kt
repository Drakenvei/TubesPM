package com.example.tubespm.ui.screens.siswa.exercises.tryout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.repository.ActivityRepository
import com.example.tubespm.repository.ExerciseCatalogRepository
import com.google.firebase.firestore.core.Query
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

data class TryoutCatalogItem(
    val tryout: Tryout,
    val isTaken: Boolean // true jika sudah ada di 'user_activities'
)

// State yang akan diobservasi oleh UI
data class TryoutUiState(
    val isLoading: Boolean = true,
    val tryouts: List<TryoutCatalogItem> = emptyList(),
    val error: String? = null
)

sealed class TryoutEvent {
    data class ShowToast(val message: String) : TryoutEvent()
}

@HiltViewModel
class TryoutViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository, // Hilt akan menyuntikkan ExerciseCatalogRepositoryImpl
    private val activityRepository: ActivityRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow(TryoutUiState())
    val uiState: StateFlow<TryoutUiState> = _uiState.asStateFlow()

    // untuk fitur search
    // 1. StateFlow untuk menyimpan query pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- CHANNEL EVENT ---
    private val _events = Channel<TryoutEvent>()
    val events = _events.receiveAsFlow()

    // --- STATE UNTUK DIALOG KONFIRMASI ---
    private val _showConfirmationDialog = MutableStateFlow<Tryout?>(null)
    val showConfirmationDialog = _showConfirmationDialog.asStateFlow()

    init {
        observeFilteredTryouts()
    }

    // 2. Fungsi publik untuk UI (SearchBar) untuk memperbarui query
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // 3. Fungsi ini sekarang menggabungkan data DENGAN query pencarian
    private fun observeFilteredTryouts() {
        viewModelScope.launch {
            // 1. Ambil flow dari katalog tryout (List<Tryout>)
            val tryoutsFlow = repository.getTryouts()

            // 2. Ambil flow dari aktivitas user (List<UserActivity>)
            //    dan ubah menjadi Set<String> berisi ID yang sudah diambil
            val takenTryoutIdsFlow = activityRepository.getMyTryoutActivities()
                .map { activities ->
                    activities.map { it.activityRefId }.toSet()
                }

            // 3. Gabungkan (combine) TIGA flow: Katalog, ID yang diambil, dan Query Pencarian
            combine(tryoutsFlow, takenTryoutIdsFlow, _searchQuery) { tryouts, takenIds, query ->

                val sortedTryouts = tryouts.sortedByDescending { it.createdAt }

                // A. Map data mentah Tryout ke TryoutCatalogItem
                val catalogItems = sortedTryouts.map { tryout ->
                    TryoutCatalogItem(
                        tryout = tryout,
                        isTaken = takenIds.contains(tryout.id) // Cek apakah ID-nya ada di Set
                    )
                }

                // B. Filter berdasarkan query
                if (query.isBlank()) {
                    catalogItems
                } else {
                    catalogItems.filter { item ->
                        // Cek apakah Title mengandung query ATAU Code mengandung query
                        val titleMatch = item.tryout.title.contains(query, ignoreCase = true)
                        val codeMatch = item.tryout.code.contains(query, ignoreCase = true)

                        titleMatch || codeMatch
                    }
                }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { filteredItems ->
                    // Update UI state dengan daftar gabungan yang sudah final
                    _uiState.update {
                        it.copy(isLoading = false, tryouts = filteredItems)
                    }
                }
        }
    }

    // --- FUNGSI UNTUK MEMICU DIALOG ---
    fun onTakeTryoutClicked(tryout: Tryout) {
        _showConfirmationDialog.value = tryout
    }

    fun onDismissDialog() {
        _showConfirmationDialog.value = null
    }

    /**
     * Dipanggil saat user menekan tombol "Ambil Tryout" di dialog.
     */
    fun onConfirmTakeTryout() {
        val tryout = _showConfirmationDialog.value ?: return
        _showConfirmationDialog.value = null // Tutup dialog

        viewModelScope.launch {
            try {
                activityRepository.addTryoutActivity(tryout)
                // Kirim Event Toast Sukses
                _events.send(TryoutEvent.ShowToast("Tryout berhasil diambil! Cek menu Activity."))
            } catch (e: Exception) {
                Log.e("TryoutViewModel", "Gagal: ${e.message}")
                _events.send(TryoutEvent.ShowToast("Gagal mengambil tryout."))
            }
        }
    }
}