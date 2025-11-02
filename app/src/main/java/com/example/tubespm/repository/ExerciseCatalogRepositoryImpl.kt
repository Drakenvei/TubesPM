package com.example.tubespm.repository

import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Tryout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseCatalogRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ExerciseCatalogRepository {
    override fun getTryouts(): Flow<List<Tryout>> {
        // .snapshots() secara otomatis memberikan data real-time (Flow)
        return db.collection("tryouts")
            .whereEqualTo("status", "active")
            .snapshots()
            .map { snapshots ->
                // Mengubah dokumen Firestore menjadi list data class Tryout
                snapshots.toObjects(Tryout::class.java)
            }
    }

    override fun getLatihanSoal(): Flow<List<LatihanSoal>> {
        return db.collection("latihan_soal")
            .whereEqualTo("status", "active")
            .snapshots()
            .map { snapshots ->
                snapshots.toObjects(LatihanSoal::class.java)
            }
    }
}