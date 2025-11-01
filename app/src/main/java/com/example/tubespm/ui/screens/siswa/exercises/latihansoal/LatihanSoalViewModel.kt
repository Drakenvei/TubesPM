package com.example.tubespm.ui.screens.siswa.exercises.latihansoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.repository.ExerciseCatalogRepository
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LatihanSoalUiState(
    val isLoading: Boolean = true,
    val latihanSoal: List<LatihanSoal> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LatihanSoalViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LatihanSoalUiState())
    val uiState: StateFlow<LatihanSoalUiState> = _uiState.asStateFlow()

    // 1. StateFlow untuk menyimpan query pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
            // Ambil aliran data (Flow) dari repository
            val latihanSoalFlow = repository.getLatihanSoal()
                // Ambil aliran data (Flow) dari repository
                combine(latihanSoalFlow, _searchQuery) { latihanSoal, query ->
                    // Blok ini akan berjalan setiap kali 'latihanSoal' ATAU 'query' berubah
                    if (query.isBlank()) {
                        // Jika query kosong, tampilkan semua tryout
                        latihanSoal
                    } else {
                        // Jika query ada, filter daftar berdasarkan judul
                        latihanSoal.filter {
                            it.title.contains(query, ignoreCase = true)
                        }
                    }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
                .collect { filteredTryouts ->
                    _uiState.update {
                        // Update UI state dengan daftar yang sudah difilter
                        it.copy(isLoading = false, latihanSoal = filteredTryouts)
                    }
                }
        }
    }
}