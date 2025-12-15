package com.example.tubespm.ui.screens.admin.management

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ======================================================
// DATA MODEL DIPERBARUI (Menyesuaikan kebutuhan UI Siswa)
// ======================================================
data class PaketSoal(
    val id: String,
    val nama: String,
    val subtest: String,    // Tambahan: Untuk Tag kategori (misal: Penalaran Umum)
    val totalSoal: Int,     // Tambahan: Jumlah total soal langsung
    val code: String,       // Tambahan: Kode paket (opsional, untuk info tambahan)
    val isActive: Boolean = true  // Status aktif/nonaktif
)

data class ManajemenLatihanUiState(
    val isLoading: Boolean = true,
    val paketSoalList: List<PaketSoal> = emptyList(),
    val error: String? = null
)

class ManajemenLatihanSoalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ManajemenLatihanUiState())
    val uiState: StateFlow<ManajemenLatihanUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        observeLatihanSoalData()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun observeLatihanSoalData() {
        viewModelScope.launch {
            val firestoreFlow = getLatihanSoalFromFirestore()

            combine(firestoreFlow, _searchQuery) { list, query ->
                val filtered = if (query.isBlank()) {
                    list
                } else {
                    list.filter {
                        it.nama.contains(query, ignoreCase = true) ||
                                it.subtest.contains(query, ignoreCase = true)
                    }
                }
                // Sort: active items first, then inactive items
                filtered.sortedByDescending { it.isActive }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { filteredList ->
                    _uiState.update {
                        it.copy(isLoading = false, paketSoalList = filteredList, error = null)
                    }
                }
        }
    }

    private fun getLatihanSoalFromFirestore(): Flow<List<PaketSoal>> = callbackFlow {
        val db = Firebase.firestore
        val listener = db.collection("latihan_soal")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val results = snapshot.documents.mapNotNull { doc ->
                        try {
                            val lat = doc.toObject(LatihanSoal::class.java)

                            // Filter item deleted
                            if (lat?.status == "deleted") return@mapNotNull null

                            lat?.let {
                                // MAPPING DATA BARU
                                PaketSoal(
                                    id = it.id,
                                    nama = it.title.ifEmpty { "Latihan Tanpa Judul" },
                                    subtest = it.subtest.ifEmpty { "Umum" }, // Ambil subtest
                                    totalSoal = it.questionCount,
                                    code = it.code,
                                    isActive = it.status == "active"
                                )
                            }
                        } catch (err: Exception) {
                            Log.e("ManajemenLatihanVM", "❌ Skip Dokumen ID: ${doc.id}", err)
                            null
                        }
                    }
                    trySend(results)
                }
            }
        awaitClose { listener.remove() }
    }
}