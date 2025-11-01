package com.example.tubespm.ui.screens.siswa.exercises.tryout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.repository.ExerciseCatalogRepository
import com.google.firebase.firestore.core.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// State yang akan diobservasi oleh UI
data class TryoutUiState(
    val isLoading: Boolean = true,
    val tryouts: List<Tryout> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TryoutViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository // Hilt akan menyuntikkan ExerciseCatalogRepositoryImpl
) : ViewModel(){
    private val _uiState = MutableStateFlow(TryoutUiState())
    val uiState: StateFlow<TryoutUiState> = _uiState.asStateFlow()

    // untuk fitur search
    // 1. StateFlow untuk menyimpan query pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
            // Ambil aliran data (Flow) dari repository
            val tryoutsFlow = repository.getTryouts()
            // Gabungkan (combine) aliran data dengan aliran query pencarian
            combine(tryoutsFlow, _searchQuery) { tryouts, query ->
                // Blok ini akan berjalan setiap kali 'tryouts' ATAU 'query' berubah
                if (query.isBlank()) {
                    // Jika query kosong, tampilkan semua tryout
                    tryouts
                } else {
                    // Jika query ada, filter daftar berdasarkan judul
                    tryouts.filter {
                        it.title.contains(query, ignoreCase = true)
                    }
                }
            }
                .catch { e ->
                    // Tangani error jika gagal
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { filteredTryouts ->
                    // Update UI state dengan daftar yang sudah difilter
                    _uiState.update {
                        it.copy(isLoading = false, tryouts = filteredTryouts)
                    }
                }
        }
    }
}