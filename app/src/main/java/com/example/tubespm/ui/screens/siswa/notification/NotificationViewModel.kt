package com.example.tubespm.ui.screens.siswa.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        val uid = auth.currentUser?.uid ?: return

        // Listen Real-time
        db.collection("users").document(uid)
            .collection("notifications")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                // Handle errors
                if (e != null) {
                    _isLoading.value = false
                    return@addSnapshotListener // untuk menghentikan fungsi jika ada error
                }

                if (snapshot != null) {
                    val items = snapshot.documents.map { doc ->
                        val timestamp = doc.getTimestamp("date")
                        NotificationItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            time = doc.getString("time") ?: "", // atau format dari timestamp
                            date = timestamp?.toDate() ?: Date(),
                            isRead = doc.getBoolean("isRead") ?: false,
                            category = NotificationCategory.GENERAL // Sesuaikan logika kategori
                        )
                    }
                    _notifications.value = items
                    _isLoading.value = false
                }
            }
    }

    // --- FUNGSI MARK AS READ (SATU ITEM) ---
    fun markAsRead(notificationId: String) {
        val uid = auth.currentUser?.uid ?:return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .collection("notifications").document(notificationId)
                    .update("isRead", true)
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- FUNGSI MARK ALL AS READ (SEMUA HARI INI/KEMARIN) ---
    fun markAllAsRead(notificationIds: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val batch = db.batch()
            val collectionRef = db.collection("users").document(uid).collection("notifications")

            notificationIds.forEach { id ->
                val docRef = collectionRef.document(id)
                batch.update(docRef, "isRead", true)
            }

            try {
                batch.commit().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}