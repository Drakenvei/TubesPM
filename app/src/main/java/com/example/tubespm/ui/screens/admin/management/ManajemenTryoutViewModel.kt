package com.example.tubespm.ui.screens.admin.management

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FilterStatus { ALL, ACTIVE, INACTIVE }

// State UI khusus untuk screen Admin Manajemen Tryout
data class ManajemenTryoutUiState(
    val isLoading: Boolean = true,
    val tryoutPackages: List<TryoutPackage> = emptyList(),
    val error: String? = null,
    val filterStatus: FilterStatus = FilterStatus.ALL
)

class ManajemenTryoutViewModel : ViewModel() {

    // 1. State Utama
    private val _uiState = MutableStateFlow(ManajemenTryoutUiState())
    val uiState: StateFlow<ManajemenTryoutUiState> = _uiState.asStateFlow()

    // 2. State Pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // StateFlow terpisah untuk Filter
    private val _filterStatus = MutableStateFlow(FilterStatus.ALL)

    init {
        // Menggabungkan data tryout dari Firestore dengan query pencarian
        viewModelScope.launch {
            combine(
                observeTryoutData(),
                _searchQuery,
                _filterStatus
            ) { packages, query, filter ->

                // 1. Filter Search
                var result = if (query.isBlank()) packages else packages.filter {
                    it.name.contains(query, ignoreCase = true) || it.code.contains(query, ignoreCase = true)
                }

                // 2. Filter Status
                result = when (filter) {
                    FilterStatus.ALL -> result
                    FilterStatus.ACTIVE -> result.filter { it.isActive }
                    FilterStatus.INACTIVE -> result.filter { !it.isActive }
                }

                // 3. Sorting DEFAULT (Terbaru Paling Atas)
                result.sortedByDescending { it.createdAt }

            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .collect { filteredPackages ->
                _uiState.update {
                    it.copy(
                        tryoutPackages = filteredPackages,
                        isLoading = false,
                        error = null,
                        filterStatus = _filterStatus.value
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: FilterStatus) {
        _filterStatus.value = status
    }

    private fun observeTryoutData(): Flow<List<TryoutPackage>> = callbackFlow {
        val db = Firebase.firestore
        val listener = db.collection("tryouts")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e) // Jika error koneksi, tutup flow dengan error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val results = snapshot.documents.mapNotNull { doc ->
                        try {
                            val tryout = doc.toObject(Tryout::class.java)

                            // Skip jika status == "deleted" (Soft Delete)
                            if (tryout?.status == "deleted") return@mapNotNull null

                            tryout?.let {
                                // Logic Mapping: Tryout (Domain) -> TryoutPackage (UI)
                                // Asumsi: Tryout.kt memiliki properti 'sections'
                                val tpsSection = it.sections.find { sec ->
                                    sec.sectionId.contains("TPS", ignoreCase = true) || sec.sectionName.contains("TPS", ignoreCase = true)
                                }
                                val literasiSection = it.sections.find { sec ->
                                    sec.sectionId.contains("Literasi", ignoreCase = true) || sec.sectionName.contains("Literasi", ignoreCase = true)
                                }

                                TryoutPackage(
                                    id = it.id,
                                    code = it.code, // <-- PERBAIKAN: Parameter 'code' ditambahkan di sini
                                    name = it.title.ifEmpty { "Tanpa Judul (${it.code})" },
                                    isActive = it.status == "active",
                                    tpsSoal = tpsSection?.sectionQuestionCount ?: 0,
                                    tpsMenit = tpsSection?.sectionDuration ?: 0,
                                    literasiSoal = literasiSection?.sectionQuestionCount ?: 0,
                                    literasiMenit = literasiSection?.sectionDuration ?: 0,
                                    takenCount = it.takenCount,
                                    createdAt = it.createdAt
                                )
                            }
                        } catch (err: Exception) {
                            Log.e("ManajemenVM", "❌ Skip Dokumen ID: ${doc.id}. Format salah.", err)
                            null // Skip dokumen rusak
                        }
                    }
                    trySend(results) // Kirim data bersih ke Flow
                }
            }

        awaitClose { listener.remove() } // Bersihkan listener saat ViewModel hancur
    }
}