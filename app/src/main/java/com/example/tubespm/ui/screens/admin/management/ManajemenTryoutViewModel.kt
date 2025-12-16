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

// State UI khusus untuk screen Admin Manajemen Tryout
data class ManajemenTryoutUiState(
    val isLoading: Boolean = true,
    val tryoutPackages: List<TryoutPackage> = emptyList(),
    val error: String? = null
)

class ManajemenTryoutViewModel : ViewModel() {

    // 1. State Utama
    private val _uiState = MutableStateFlow(ManajemenTryoutUiState())
    val uiState: StateFlow<ManajemenTryoutUiState> = _uiState.asStateFlow()

    // 2. State Pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        observeTryoutData()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun observeTryoutData() {
        viewModelScope.launch {
            // A. Ambil data Realtime dari Firestore sebagai Flow
            val firestoreFlow = getTryoutsFromFirestore()

            // B. Gabungkan (Combine) data Firestore dengan Query Pencarian
            combine(firestoreFlow, _searchQuery) { packages, query ->
                if (query.isBlank()) {
                    packages
                } else {
                    packages.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { filteredPackages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tryoutPackages = filteredPackages,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Mengambil data dari Firestore dan mengubahnya menjadi Flow List<TryoutPackage>.
     * Termasuk logika try-catch untuk menangani data "sampah" (tipe data salah).
     */
    private fun getTryoutsFromFirestore(): Flow<List<TryoutPackage>> = callbackFlow {
        val db = Firebase.firestore
        val listener = db.collection("tryouts")
            // [QUERY FILTER] Hanya ambil yang statusnya BUKAN 'deleted'
            // Kita bisa menggunakan whereIn atau whereNotEqualTo (jika field pasti ada)
            // Cara paling aman & kompatibel dengan data lama (field null): Ambil semua, filter di client
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e) // Jika error koneksi, tutup flow dengan error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val results = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Coba convert, handle error jika tipe data 'id' salah
                            val tryout = doc.toObject(Tryout::class.java)

                            // Skip jika status == "deleted"
                            if (tryout?.status == "deleted") return@mapNotNull null

                            tryout?.let {
                                // Logic Mapping: Tryout (Domain) -> TryoutPackage (UI)
                                val tpsSection = it.sections.find { sec ->
                                    sec.sectionId.contains("TPS", ignoreCase = true) || sec.sectionName.contains("TPS", ignoreCase = true)
                                }
                                val literasiSection = it.sections.find { sec ->
                                    sec.sectionId.contains("Literasi", ignoreCase = true) || sec.sectionName.contains("Literasi", ignoreCase = true)
                                }

                                TryoutPackage(
                                    id = it.id,
                                    name = it.title.ifEmpty { "Tanpa Judul (${it.code})" },
                                    isActive = it.status == "active",
                                    tpsSoal = tpsSection?.sectionQuestionCount ?: 0,
                                    tpsMenit = tpsSection?.sectionDuration ?: 0,
                                    literasiSoal = literasiSection?.sectionQuestionCount ?: 0,
                                    literasiMenit = literasiSection?.sectionDuration ?: 0,
                                    takenCount = it.takenCount
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