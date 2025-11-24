package com.example.tubespm.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tubespm.MainActivity
import com.example.tubespm.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ReminderWorker (
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val title = inputData.getString("TITLE") ?: "Waktunya Belajar!"
        val message = inputData.getString("MESSAGE") ?: "Jangan lupa kerjakan tryout dan latihan soal hari ini."
        val id = inputData.getInt("ID", 101)

        // Tampilkan notifikasi sistem
        showSystemNotification(title, message, id)

        // Simpan ke History (Firestore)
        saveToFirestore(title, message, "Pengingat Harian")

        return Result.success()
    }

    private fun showSystemNotification(title: String, message: String, notifId: Int){
        val context = applicationContext
        val channelId = "daily_reminder_channel"

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Pengingat Harian", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logobelut)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Otomatis hilang setelah diklik

        notificationManager.notify(notifId, builder.build())

    }

    private fun saveToFirestore(title: String, message: String, category: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val notifData = hashMapOf(
            "title" to title,
            "message" to message,
            "time" to SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
            "date" to FieldValue.serverTimestamp(), // Untuk sorting
            "isRead" to false,
            "category" to category
        )

        db.collection("users").document(uid)
            .collection("notifications")
            .add(notifData)
    }
}