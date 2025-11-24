package com.example.tubespm.ui.screens.admin.management

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
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

// State UI khusus untuk screen Admin Manajemen Latihan Soal
data class ManajemenLatihanUiState(
    val isLoading: Boolean = true,
    val paketSoalList: List<PaketSoal> = emptyList(),
    val error: String? = null
)

class ManajemenLatihanSoalViewModel : ViewModel() {

    // 1. State Utama
    private val _uiState = MutableStateFlow(ManajemenLatihanUiState())
    val uiState: StateFlow<ManajemenLatihanUiState> = _uiState.asStateFlow()

    // 2. State Pencarian
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
            // A. Ambil data Realtime dari Firestore sebagai Flow
            val firestoreFlow = getLatihanSoalFromFirestore()

            // B. Gabungkan (Combine) data Firestore dengan Query Pencarian
            combine(firestoreFlow, _searchQuery) { list, query ->
                if (query.isBlank()) {
                    list
                } else {
                    list.filter {
                        it.nama.contains(query, ignoreCase = true)
                    }
                }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { filteredList ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            paketSoalList = filteredList,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Mengambil data dari Firestore dan mengubahnya menjadi Flow List<PaketSoal>.
     * Dilengkapi Try-Catch untuk mencegah Crash akibat tipe data salah (Long vs String).
     */
    private fun getLatihanSoalFromFirestore(): Flow<List<PaketSoal>> = callbackFlow {
        val db = Firebase.firestore
        val listener = db.collection("latihan_soal")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e) // Tutup flow jika error koneksi
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val results = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Coba convert, jika id berupa Long (bukan String), ini akan throw error
                            // tapi ditangkap oleh catch di bawah, sehingga aplikasi AMAN.
                            val lat = doc.toObject(LatihanSoal::class.java)

                            lat?.let {
                                // Logic Mapping: LatihanSoal (Domain) -> PaketSoal (UI)
                                var tpsC = 0
                                var litC = 0

                                // Logic kategorisasi subtest
                                if (it.subtest.contains("Literasi", ignoreCase = true) ||
                                    it.subtest.contains("Bahasa", ignoreCase = true)) {
                                    litC = it.questionCount
                                } else {
                                    tpsC = it.questionCount
                                }

                                PaketSoal(
                                    id = it.id,
                                    nama = it.title.ifEmpty { "Latihan Soal (${it.code})" },
                                    tpsCount = tpsC,
                                    literasiCount = litC,
                                    tpsMenit = 0,
                                    literasiMenit = 0
                                )
                            }
                        } catch (err: Exception) {
                            Log.e("ManajemenLatihanVM", "❌ Skip Dokumen ID: ${doc.id}. Data rusak.", err)
                            null // Skip dokumen ini
                        }
                    }
                    trySend(results) // Kirim list bersih ke Flow
                }
            }
        awaitClose { listener.remove() }
    }
}