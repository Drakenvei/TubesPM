//package com.example.tubespm.repository
//
//import android.util.Log
//import com.example.tubespm.data.model.*
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import kotlinx.coroutines.tasks.await
//
//class TryoutRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
//
//    // Fetch all active tryouts
//    suspend fun getAllTryouts(): List<Tryout> {
//        return try {
//            val snapshot = db.collection("tryouts")
//                .whereEqualTo("status", "active")
//                .orderBy("createdAt", Query.Direction.DESCENDING)
//                .get()
//                .await()
//
//            snapshot.documents.mapNotNull { doc ->
//                try {
//                    val sectionsData = doc.get("sections") as? List<Map<String, Any>> ?: emptyList()
//                    val sections = sectionsData.map { sectionMap ->
//                        val subSectionsData = sectionMap["subSections"] as? List<Map<String, Any>> ?: emptyList()
//                        val subSections = subSectionsData.map { subMap ->
//                            SubSectionDetail(
//                                name = subMap["name"] as? String ?: "",
//                                questionCount = (subMap["questionCount"] as? Long)?.toInt() ?: 0,
//                                duration = (subMap["duration"] as? Long)?.toInt() ?: 0,
//                                kisiKisi = (subMap["kisiKisi"] as? List<String>) ?: emptyList()
//                            )
//                        }
//
//                        TryoutSection(
//                            title = sectionMap["title"] as? String ?: "",
//                            displayName = sectionMap["displayName"] as? String ?: "",
//                            totalQuestions = (sectionMap["totalQuestions"] as? Long)?.toInt() ?: 0,
//                            totalDuration = (sectionMap["totalDuration"] as? Long)?.toInt() ?: 0,
//                            subSections = subSections
//                        )
//                    }
//
//                    Tryout(
//                        id = (doc.getLong("id") ?: 0).toInt(),
//                        title = doc.getString("title") ?: "",
//                        code = doc.getString("code") ?: "",
//                        sections = sections
//                    )
//                } catch (e: Exception) {
//                    Log.e("TryoutRepository", "Error parsing tryout: ${e.message}")
//                    null
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("TryoutRepository", "Error fetching tryouts: ${e.message}")
//            emptyList()
//        }
//    }
//
//    // Fetch tryout by ID
//    suspend fun getTryoutById(tryoutId: Int): Tryout? {
//        return try {
//            val snapshot = db.collection("tryouts")
//                .whereEqualTo("id", tryoutId)
//                .limit(1)
//                .get()
//                .await()
//
//            if (snapshot.documents.isNotEmpty()) {
//                val doc = snapshot.documents[0]
//                val sectionsData = doc.get("sections") as? List<Map<String, Any>> ?: emptyList()
//                val sections = sectionsData.map { sectionMap ->
//                    val subSectionsData = sectionMap["subSections"] as? List<Map<String, Any>> ?: emptyList()
//                    val subSections = subSectionsData.map { subMap ->
//                        SubSectionDetail(
//                            name = subMap["name"] as? String ?: "",
//                            questionCount = (subMap["questionCount"] as? Long)?.toInt() ?: 0,
//                            duration = (subMap["duration"] as? Long)?.toInt() ?: 0,
//                            kisiKisi = (subMap["kisiKisi"] as? List<String>) ?: emptyList()
//                        )
//                    }
//
//                    TryoutSection(
//                        title = sectionMap["title"] as? String ?: "",
//                        displayName = sectionMap["displayName"] as? String ?: "",
//                        totalQuestions = (sectionMap["totalQuestions"] as? Long)?.toInt() ?: 0,
//                        totalDuration = (sectionMap["totalDuration"] as? Long)?.toInt() ?: 0,
//                        subSections = subSections
//                    )
//                }
//
//                Tryout(
//                    id = (doc.getLong("id") ?: 0).toInt(),
//                    title = doc.getString("title") ?: "",
//                    code = doc.getString("code") ?: "",
//                    sections = sections
//                )
//            } else null
//        } catch (e: Exception) {
//            Log.e("TryoutRepository", "Error fetching tryout by ID: ${e.message}")
//            null
//        }
//    }
//
//    // Search tryouts
//    suspend fun searchTryouts(query: String): List<Tryout> {
//        return try {
//            val allTryouts = getAllTryouts()
//            allTryouts.filter {
//                it.title.contains(query, ignoreCase = true) ||
//                        it.code.contains(query, ignoreCase = true)
//            }
//        } catch (e: Exception) {
//            Log.e("TryoutRepository", "Error searching tryouts: ${e.message}")
//            emptyList()
//        }
//    }
//}