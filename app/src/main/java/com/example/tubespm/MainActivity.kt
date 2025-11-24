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
            SiswaMainScreen()
        }

        // Root / main screen untuk ADMIN
        // AdminMainScreen biasanya berisi Scaffold + BottomNavbarAdmin + NavHost admin
        composable("admin_main") {
            AdminMainScreen(rootNavController = navController)
        }
    }
}