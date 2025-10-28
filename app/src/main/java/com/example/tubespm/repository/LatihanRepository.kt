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
            android.util.Log.d("LatihanRepository", "📡 Fetching from Firestore...")
            android.util.Log.d("LatihanRepository", "Query: collection('latihan_soal').whereEqualTo('status', 'active')")

            val snapshot = db.collection("latihan_soal")
                .whereEqualTo("status", "active")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            android.util.Log.d("LatihanRepository", "📦 Received ${snapshot.documents.size} documents from Firebase")

            snapshot.documents.forEachIndexed { index, doc ->
                android.util.Log.d("LatihanRepository", "  Doc[$index]: id=${doc.id}, data exists=${doc.exists()}")
                android.util.Log.d("LatihanRepository", "    - title: ${doc.getString("title")}")
                android.util.Log.d("LatihanRepository", "    - status: ${doc.getString("status")}")
                android.util.Log.d("LatihanRepository", "    - questionCount: ${doc.getLong("questionCount")}")
            }

            val result = snapshot.documents.mapNotNull { doc ->
                try {
                    val latihan = LatihanSoal(
                        id = (doc.getLong("id") ?: 0).toInt(),
                        title = doc.getString("title") ?: "",
                        subtest = doc.getString("subtest") ?: "",
                        questionCount = (doc.getLong("questionCount") ?: 0).toInt(),
                        kisiKisi = (doc.get("kisiKisi") as? List<String>) ?: emptyList()
                    )
                    android.util.Log.d("LatihanRepository", "✅ Parsed: ${latihan.title}")
                    latihan
                } catch (e: Exception) {
                    android.util.Log.e("LatihanRepository", "❌ Error parsing document ${doc.id}: ${e.message}", e)
                    null
                }
            }

            android.util.Log.d("LatihanRepository", "🎯 Returning ${result.size} latihan objects")
            result
        } catch (e: Exception) {
            android.util.Log.e("LatihanRepository", "💥 FATAL ERROR in getAllLatihan: ${e.message}", e)
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