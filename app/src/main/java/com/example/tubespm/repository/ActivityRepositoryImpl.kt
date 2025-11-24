package com.example.tubespm.repository

import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ActivityRepository {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun getMyTryoutActivities(): Flow<List<UserActivity>> {
        val uid = currentUserId
        if (uid == null){
            return flowOf(emptyList()) // Kembalikan list kosong jika user tidak login
        }

        return db.collection("user_activities")
            .whereEqualTo("userId", uid)
            .whereEqualTo("type", "tryout")
            .snapshots() //Dapatkan data realtime
            .map { snapshots ->
                snapshots.toObjects(UserActivity::class.java)
            }
    }

    override fun getMyLatihanActivities(): Flow<List<UserActivity>> {
        val uid = currentUserId
        if (uid == null) {
            return flowOf(emptyList())
        }

        return db.collection("user_activities")
            .whereEqualTo("userId", uid)
            .whereEqualTo("type", "latihan_soal") // <-- Filter "latihan"
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(UserActivity::class.java)
            }
    }

    override fun getGlobalRecentActivities(limit: Long): Flow<List<UserActivity>> {
        // Hitung tanggal 30 hari yang lalu
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val oneMonthAgo = cal.time

        // Query Global (Tanpa filter userId)
        return db.collection("user_activities")
            .whereEqualTo("type", "tryout")
            .whereGreaterThan("startedAt", oneMonthAgo)
            .orderBy("startedAt", Query.Direction.DESCENDING)
            .limit(limit) // Batasi jumlah agar tidak boros kuota (misal ambil 100 sampel terakhir)
            .snapshots()
            .map { snapshots ->
                snapshots.toObjects(UserActivity::class.java)
            }
    }

    override suspend fun addTryoutActivity(tryout: Tryout) {
        val uid = currentUserId ?: return // Jangan lakukan apa-apa jika user tidak login

        // 1. Buat data baru untuk 'user_activities'
        val newActivity = mapOf(
            "userId" to uid,
            "type" to "tryout",
            "activityRefId" to tryout.id, // ID dari dokumen tryout
            "activityTitle" to tryout.title, // Ambil judul tryout
            "status" to "not_started", // Status awal
            "score" to 0,
            "correctCount" to 0,
            "startedAt" to FieldValue.serverTimestamp()
        )

        // 2. Simpan ke Firestore
        db.collection("user_activities").add(newActivity).await()
    }

    override suspend fun addLatihanActivity(latihan: LatihanSoal) {
        val uid = currentUserId ?: return // Jangan lakukan apa-apa jika user tidak login

        // Buat data baru untuk 'user_activities'
        val newActivity = mapOf(
            "userId" to uid,
            "type" to "latihan_soal", // <-- Tipe yang benar
            "activityRefId" to latihan.id, // <-- ID dari dokumen latihan_soal
            "activityTitle" to latihan.title,
            "status" to "not_started",
            "score" to 0,
            "correctCount" to 0,
            "answeredQuestionCount" to 0,
            "startedAt" to FieldValue.serverTimestamp()
        )

        // Simpan ke firestore
        db.collection("user_activities").add(newActivity).await()
    }

    override suspend fun cancelTryoutActivity(activityId: String) {
        db.collection("user_activities")
            .document(activityId)
            .delete()
            .await()
    }

    override suspend fun cancelLatihanActivity(activityId: String) {
        db.collection("user_activities")
            .document(activityId)
            .delete()
            .await()
    }
}