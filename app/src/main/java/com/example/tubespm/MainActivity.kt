package com.example.tubespm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.screens.admin.AdminMainScreen
import com.example.tubespm.ui.screens.auth.AuthenticationScreen
import com.example.tubespm.ui.screens.getstarted.GetStartedPage
import com.example.tubespm.ui.screens.siswa.SiswaMainScreen
import com.example.tubespm.ui.screens.splash.SplashScreen
import com.example.tubespm.ui.theme.TubesPMTheme
import dagger.hilt.android.AndroidEntryPoint

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

    // Hapus auth dan db dari sini, ViewModel yang akan menanganinya
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("get_started") { GetStartedPage(onClick = { navController.navigate("auth") }) }

        // Composable sekarang memanggil layar yang sudah bersih
        composable("auth") {
            AuthenticationScreen(
                // Callback onAuthSuccess sekarang menerima 'role'
                onAuthSuccess = { role ->
                    // Callback onAuthSuccess sekarang menerima 'role'
                    val destination = when (role){
                        "admin" -> "admin_main"
                        "siswa" -> "siswa_main"
                        else -> "siswa_main" // Default ke siswa jika role tidak dikenali
                    }
                    navController.navigate(destination) {
                        // Hapus tumpukan navigasi ke 'auth' agar tidak bisa kembali
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("siswa_main") {
            SiswaMainScreen()
        }
        composable("admin_main"){
            AdminMainScreen()
        }
    }
}