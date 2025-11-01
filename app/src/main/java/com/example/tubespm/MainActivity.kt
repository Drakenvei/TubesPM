package com.example.tubespm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.MainScreen
import com.example.tubespm.ui.screens.auth.LoginScreen
import com.example.tubespm.ui.screens.auth.RegisterScreen
import com.example.tubespm.ui.screens.getstarted.GetStartedPage
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
        composable("get_started") { GetStartedPage(onClick = { navController.navigate("login") }) }

        // Composable sekarang memanggil layar yang sudah bersih
        composable("login") {
            LoginScreen(navController = navController) // ViewModel diambil di dalam
        }
        composable("register") {
            RegisterScreen(navController = navController) // ViewModel diambil di dalam
        }
        composable("main") { MainScreen() }
    }
}