package com.example.tubespm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.tubespm.ui.screens.admin.AdminMainScreen
import com.example.tubespm.ui.screens.auth.AuthenticationScreen
import com.example.tubespm.ui.screens.getstarted.GetStartedPage
import com.example.tubespm.ui.screens.siswa.SiswaMainScreen
import com.example.tubespm.ui.screens.splash.SplashScreen
import com.example.tubespm.ui.theme.TubesPMTheme
import com.example.tubespm.worker.ReminderWorker
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Minta Izin Notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS )!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Jadwalkan WorkManager (Contoh: Jam 09:00 Pagi)
        scheduleDailyNotification(9, 0, 101, "Notif Pagi", "Selamat Pagi!", "Ayo mulai hari dengan mengerjakan latihan")
        scheduleDailyNotification(14, 5, 102, "Notif Siang", "Selamat Siang!", "Ayo gas kerjakan tryoutnya")

        // INI UNTUK TESTING WORKMANAGER
//        val testRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
//            .setInitialDelay(10, TimeUnit.SECONDS) // Jalan 10 detik lagi
//            .setInputData(
//                workDataOf(
//                    "TITLE" to "Tes WorkManager",
//                    "MESSAGE" to "Berhasil jalan dalam 10 detik!",
//                    "ID" to 123
//                )
//            )
//            .build()
//
//        WorkManager.getInstance(this).enqueue(testRequest)

        // Subscribe FCM Topic
        FirebaseMessaging.getInstance().subscribeToTopic("students")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Berhasil subscribe ke students")
                } else {
                    Log.e("FCM", "Gagal subscribe")
                }
            }

        setContent {
            TubesPMTheme {
                AppNavigation()
            }
        }
    }

    private fun  scheduleDailyNotification(hour: Int, minute: Int, id: Int, workName: String, title: String, message: String) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, hour)
        dueDate.set(Calendar.MINUTE, minute)
        dueDate.set(Calendar.SECOND, 0)

        // Jika tanggal sudah lewat, tambahkan 1 hari
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        // Hitung selisih waktu dalam milidetik
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val data = Data.Builder()
            .putString("TITLE", title)
            .putString("MESSAGE", message)
            .putInt("ID", id)
            .build()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )

        // Log untuk memastikan jadwal terpasang (Cek di Logcat)
        Log.d("WorkManager", "Jadwal $workName dipasang untuk $timeDiff ms lagi (ID: $id)")
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Splash pertama kali
        composable("splash") {
            SplashScreen(navController = navController)
        }

        // Halaman get started
        composable("get_started") {
            GetStartedPage(
                onClick = {
                    navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Halaman login / register
        composable("auth") {
            AuthenticationScreen(
                onAuthSuccess = { role ->
                    val destination = when (role) {
                        "admin" -> "admin_main"
                        "siswa" -> "siswa_main"
                        else -> "siswa_main"
                    }

                    navController.navigate(destination) {
                        // buang history auth supaya tidak bisa back ke login
                        popUpTo("auth") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Root / main screen untuk SISWA
        // SiswaMainScreen biasanya berisi Scaffold + BottomNavBar + NavHost siswa
        composable("siswa_main") {
            SiswaMainScreen(rootNavController = navController)
        }

        // Root / main screen untuk ADMIN
        // AdminMainScreen biasanya berisi Scaffold + BottomNavbarAdmin + NavHost admin
        composable("admin_main") {
            AdminMainScreen(rootNavController = navController)
        }
    }
}