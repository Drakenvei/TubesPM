package com.example.tubespm.repository

import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Tryout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseCatalogRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ExerciseCatalogRepository {
    override fun getTryouts(): Flow<List<Tryout>> {
        // Ambil dari koleksi 'tryouts'
        // Filter hanya yang statusnya 'active'
        return db.collection("tryouts")
            .whereEqualTo("status", "active")
            .snapshots() // Ambil data secara real-time
            .map { snapshot ->
                // Konversi dokumen Firestore ke List<Tryout>
                // Ini berfungsi karena data class Tryout.kt
                // sekarang cocok dengan struktur database
                snapshot.toObjects(Tryout::class.java)
            }
    }

    override fun getLatihanSoal(): Flow<List<LatihanSoal>> {
        // Implementasi serupa untuk latihan soal
        return db.collection("latihan_soal")
            .whereEqualTo("status", "active")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(LatihanSoal::class.java)
            }
    }
}