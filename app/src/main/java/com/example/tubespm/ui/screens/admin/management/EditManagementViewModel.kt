package com.example.tubespm.ui.screens.admin.management

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Model UI khusus untuk list di dalam Dialog
data class TryoutSectionUiModel(
    val id: String, // subtestId (Bukan sectionId!)
    val title: String, // subtestName
    val type: String, // "TPS" atau "Literasi" (Diambil dari parent section)
    val timeMinutes: Int, // duration
    val questionCount: Int, // questionCount
    val parentSectionId: String, // "tps" atau "literasi" (Penting untuk referensi)
    val topicsString: String = "", //untuk menampung kisi-kisi
    val actualCount: Int = 0
)

data class EditManagementUiState(
    val isLoading: Boolean = true,
    val sections: List<TryoutSectionUiModel> = emptyList(), //Ini sebenarnya list of Subtests
    val error: String? = null
)

class EditManagementViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditManagementUiState())
    val uiState: StateFlow<EditManagementUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore

    /**
     * Mengambil detail sections dari dokumen Tryout spesifik
     */
    fun loadSections(tryoutId: String) {
        _uiState.update { it.copy(isLoading = true) }

        db.collection("tryouts").document(tryoutId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    viewModelScope.launch {
                        try {
                            val tryout = snapshot.toObject(Tryout::class.java)

                            // --- FLATTEING DATA ---
                            // Mengubah struktur nested (Section -> Subtests) menjadi satu list datar
                            val flatList = mutableListOf<TryoutSectionUiModel>()

                            tryout?.sections?.forEach { section ->
                                // Logic to determine type label (TPS/Literasi)
                                val typeLabel = if (section.sectionId.equals("tps", ignoreCase = true)) "TPS" else "Literasi"

                                section.subtests.forEach { subtest ->

                                    // Proses Mapping Topics ke String
                                    // Mengubah List<Topic> menjadi String "Aljabar, Geometri"
                                    val topicsStr = subtest.topics.joinToString(", ") { it.name }

                                    val countSnapshot = db.collection("tryouts")
                                        .document(tryoutId)
                                        .collection("questions")
                                        .whereEqualTo("subtestId", subtest.subtestId)
                                        .count()
                                        .get(AggregateSource.SERVER)
                                        .await()

                                    val realCount = countSnapshot.count.toInt()

                                    flatList.add(
                                        TryoutSectionUiModel(
                                            // ERROR IS LIKELY HERE:
                                            // Ensure you are using 'subtest.subtestId' (e.g., "pu"), NOT 'section.sectionId' (e.g., "tps")
                                            id = subtest.subtestId,
                                            title = subtest.subtestName,
                                            type = typeLabel,
                                            timeMinutes = subtest.duration,
                                            questionCount = subtest.questionCount,
                                            parentSectionId = section.sectionId,
                                            topicsString = topicsStr,
                                            actualCount = realCount
                                        )
                                    )
                                }
                            }

                            _uiState.update {
                                it.copy(isLoading = false, sections = flatList, error = null)
                            }
                        } catch (err: Exception) {
                            Log.e("EditManagementVM", "Error parsing data", err)
                            _uiState.update { it.copy(isLoading = false, error = "Data corrupt") }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Dokumen tidak ditemukan") }
                }
            }
    }

    /**
     * Mengubah status tryout menjadi inactive
     */
    fun deactivatePackage(tryoutId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            db.collection("tryouts").document(tryoutId)
                .update("status", "inactive")
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("EditManagementVM", "Gagal update status", e)
                }
        }
    }

    /**
     * Mengubah status tryout menjadi active
     */
    fun activatePackage(tryoutId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentSections = _uiState.value.sections

        // Validasi: Pastikan data sudah termuat
        if (currentSections.isEmpty()) {
            onError("Data subtest belum dimuat. Harap tunggu.")
            return
        }

        // Validasi: Cek apakah ada subtest yang jumlah soalnya KURANG dari target
        val incompleteSections = currentSections.filter { it.actualCount < it.questionCount }

        if (incompleteSections.isNotEmpty()) {
            // Jika ada yang belum target, tolak aktivasi
            val names = incompleteSections.joinToString(", ") { it.title }
            onError("Gagal: Subtest berikut belum memenuhi target soal: $names")
            return
        }

        viewModelScope.launch {
            db.collection("tryouts").document(tryoutId)
                .update("status", "active")
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("EditManagementVM", "Gagal activate status", e)
                }
        }
    }

    fun deleteSubtest(
        tryoutId: String,
        subtestIdToDelete: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Hapus Soal-soal Terkait (Batch Delete)
                // Kita cari dulu semua soal dengan subtestId ini
                val questionsRef = db.collection("tryouts").document(tryoutId).collection("questions")
                val querySnapshot = questionsRef.whereEqualTo("subtestId", subtestIdToDelete).get().await()

                // Batch write untuk menghapus banyak dokumen sekaligus
                val batch = db.batch()
                for (document in querySnapshot.documents) {
                    batch.delete(document.reference)
                }
                // Eksekusi hapus soal
                batch.commit().await()

                // 2. Update Dokumen Tryout (Hapus Subtest dari Array)
                db.runTransaction { transaction ->
                    val tryoutRef = db.collection("tryouts").document(tryoutId)
                    val snapshot = transaction.get(tryoutRef)
                    val tryout = snapshot.toObject(Tryout::class.java) ?: throw Exception("Tryout tidak ditemukan")

                    val currentSections = tryout.sections.toMutableList()
                    var isChanged = false

                    // Cari section yang berisi subtest ini
                    for (i in currentSections.indices) {
                        val section = currentSections[i]
                        val updatedSubtests = section.subtests.filter { it.subtestId != subtestIdToDelete }

                        if (updatedSubtests.size != section.subtests.size) {
                            // Ada yang dihapus
                            currentSections[i] = section.copy(subtests = updatedSubtests)
                            isChanged = true
                        }
                    }

                    // Hapus section jika kosong (Opsional, tapi rapi)
                    // currentSections.removeAll { it.subtests.isEmpty() }

                    if (isChanged) {
                        // Hitung ulang total durasi & soal
                        val newTotalDuration = currentSections.sumOf { sec -> sec.subtests.sumOf { it.duration } }
                        val newTotalQuestionCount = currentSections.sumOf { sec -> sec.subtests.sumOf { it.questionCount } }

                        transaction.update(tryoutRef, mapOf(
                            "sections" to currentSections,
                            "totalDuration" to newTotalDuration,
                            "totalQuestionCount" to newTotalQuestionCount
                        ))
                    }
                }.await()

                onSuccess()
            } catch (e: Exception) {
                Log.e("EditManagementVM", "Gagal hapus subtest", e)
                onError("Gagal menghapus subtest: ${e.message}")
            }
        }
    }

    /**
     * SOFT DELETE: Mengubah status paket menjadi 'deleted'.
     * Data tidak dihapus fisik, tapi hilang dari list utama.
     */
    fun deletePackage(
        packageId: String,
        type: String, // "tryout" atau "latihan_soal"
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val collectionName = if (type == "tryout") "tryouts" else "latihan_soal"

                // Update status field menjadi 'deleted'
                db.collection(collectionName).document(packageId)
                    .update("status", "deleted")
                    .await()

                onSuccess()

            } catch (e: Exception) {
                Log.e("EditManagementVM", "Gagal hapus paket (soft delete)", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                onError("Gagal menghapus paket: ${e.message}")
            }
        }
    }
}