package com.example.tubespm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.screens.admin.AdminMainScreen
import com.example.tubespm.ui.screens.admin.AdminHomeScreen   // ⬅️ import homepage admin
import com.example.tubespm.ui.screens.auth.AuthenticationScreen
import com.example.tubespm.ui.screens.getstarted.GetStartedPage
import com.example.tubespm.ui.screens.siswa.SiswaMainScreen
import com.example.tubespm.ui.screens.splash.SplashScreen
import com.example.tubespm.ui.theme.TubesPMTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay   // ⬅️ untuk delay 3–5 detik

// Tambahkan anotasi ini untuk Hilt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TubesPMTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ViewModel yang akan menanganinya
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }

        composable("get_started") {
            GetStartedPage(onClick = { navController.navigate("auth") })
        }

        composable("auth") {
            AuthenticationScreen(
                // Callback onAuthSuccess menerima 'role'
                onAuthSuccess = { role ->
                    val destination = when (role) {
                        "admin" -> "admin_main"   // ⬅️ admin ke admin_main dulu (splash)
                        "siswa" -> "siswa_main"
                        else -> "siswa_main"      // default ke siswa
                    }
                    navController.navigate(destination) {
                        // Hapus 'auth' dari backstack
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Homepage siswa
        composable("siswa_main") {
            SiswaMainScreen()
        }

        // Splash khusus admin (3–5 detik)
        composable("admin_main") {
            AdminMainScreen()

            LaunchedEffect(Unit) {
                // 3000L = 3 detik, kalau mau 5 detik ubah ke 5000L
                delay(3000L)

                navController.navigate("admin_home") {
                    // hapus admin_main dari backstack
                    popUpTo("admin_main") { inclusive = true }
                }
            }
        }

        // Homepage admin sesungguhnya (dari homepage.kt)
        composable("admin_home") {
            AdminHomeScreen()
        }
    }
}
