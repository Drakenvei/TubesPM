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
    val adminPhotoBase64: String = ""
)

class AdminHomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {
        observeUserData() // Ambil foto & nama realtime
        loadStatistics()  // Ambil angka statistik
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
                val tryoutSnap = db.collection("tryouts").count().get(AggregateSource.SERVER).await()
                val latihanSnap = db.collection("latihan_soal").count().get(AggregateSource.SERVER).await()
                val siswaSnap = db.collection("users").count().get(AggregateSource.SERVER).await()
                val aktivitasSnap = db.collection("user_activities").count().get(AggregateSource.SERVER).await()

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
}