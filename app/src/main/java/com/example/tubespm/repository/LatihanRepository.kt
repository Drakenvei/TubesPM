package com.example.tubespm.repository

import android.util.Log
import com.example.tubespm.data.model.LatihanSoal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class LatihanRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // Fetch all active latihan soal
    suspend fun getAllLatihan(): List<LatihanSoal> {
        return try {
            val snapshot = db.collection("latihan_soal")
                .whereEqualTo("status", "active")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    LatihanSoal(
                        id = (doc.getLong("id") ?: 0).toInt(),
                        title = doc.getString("title") ?: "",
                        subtest = doc.getString("subtest") ?: "",
                        questionCount = (doc.getLong("questionCount") ?: 0).toInt(),
                        kisiKisi = (doc.get("kisiKisi") as? List<String>) ?: emptyList()
                    )
                } catch (e: Exception) {
                    Log.e("LatihanRepository", "Error parsing latihan: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LatihanRepository", "Error fetching latihan: ${e.message}")
            emptyList()
        }
    }

    // Fetch latihan by ID
    suspend fun getLatihanById(latihanId: Int): LatihanSoal? {
        return try {
            val snapshot = db.collection("latihan_soal")
                .whereEqualTo("id", latihanId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents[0]
                LatihanSoal(
                    id = (doc.getLong("id") ?: 0).toInt(),
                    title = doc.getString("title") ?: "",
                    subtest = doc.getString("subtest") ?: "",
                    questionCount = (doc.getLong("questionCount") ?: 0).toInt(),
                    kisiKisi = (doc.get("kisiKisi") as? List<String>) ?: emptyList()
                )
            } else null
        } catch (e: Exception) {
            Log.e("LatihanRepository", "Error fetching latihan by ID: ${e.message}")
            null
        }
    }

    // Search latihan soal
    suspend fun searchLatihan(query: String): List<LatihanSoal> {
        return try {
            val allLatihan = getAllLatihan()
            allLatihan.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.subtest.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e("LatihanRepository", "Error searching latihan: ${e.message}")
            emptyList()
        }
    }

    // Filter latihan by subtest
    suspend fun filterBySubtest(subtest: String): List<LatihanSoal> {
        return try {
            val snapshot = db.collection("latihan_soal")
                .whereEqualTo("status", "active")
                .whereEqualTo("subtest", subtest)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    LatihanSoal(
                        id = (doc.getLong("id") ?: 0).toInt(),
                        title = doc.getString("title") ?: "",
                        subtest = doc.getString("subtest") ?: "",
                        questionCount = (doc.getLong("questionCount") ?: 0).toInt(),
                        kisiKisi = (doc.get("kisiKisi") as? List<String>) ?: emptyList()
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LatihanRepository", "Error filtering latihan: ${e.message}")
            emptyList()
        }
    }
}