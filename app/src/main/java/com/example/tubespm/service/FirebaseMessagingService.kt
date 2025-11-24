package com.example.tubespm.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.tubespm.MainActivity
import com.example.tubespm.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "Info Baru"
        val body = message.notification?.body ?: "Cek aplikasi sekarang"

        // Tampilkan notifikasi sistem (bunyi/popup)
        showSystemNotification(title, body)

        // ❌ HAPUS baris ini: saveToFirestore(title, body, "Info Admin")
        // Karena data sudah disimpan oleh Admin di Database sebelumnya.
        // Simpan ke History (Firestore)
//        saveToFirestore(title, body, "Info Admin")
    }

    private fun showSystemNotification(title: String, message: String) {
        val channelId = "fcm_channel"
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Info Penting", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logobelut)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Ini tidak jadi digunakan karena adminlah yang akan menyimpan pesan ke notification siswa lalu menjalankan notifikasi
//    private fun saveToFirestore(title: String, message: String, category: String) {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        val db = FirebaseFirestore.getInstance()
//
//        val notifData = hashMapOf(
//            "title" to title,
//            "message" to message,
//            "time" to SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
//            "date" to FieldValue.serverTimestamp(), // Untuk sortin
//            "isRead" to false,
//            "category" to category
//        )
//
//        db.collection("users").document(uid)
//            .collection("notifications")
//            .add(notifData)
//    }
}