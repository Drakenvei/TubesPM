package com.example.tubespm.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.navigation.BottomNavbarAdmin
import com.example.tubespm.ui.screens.admin.homepage.AdminHomeScreen
import com.example.tubespm.ui.screens.admin.profile.*

@Composable
fun AdminMainScreen(
    rootNavController: NavHostController // NavController untuk navigasi Lintas-Grafik/Aplikasi Utama
) {
    val adminNavController = rememberNavController() // NavController untuk navigasi di dalam AdminMainScreen

    Scaffold(
        bottomBar = { BottomNavbarAdmin(navController = adminNavController) }
    ) { paddingValues -> // paddingValues dari Scaffold (BENAR)

        NavHost(
            navController = adminNavController,
            startDestination = "admin_home"
        ) {
            // Rute 1: Admin Home
            composable("admin_home") {
                AdminHomeScreen(paddingValues = paddingValues)
            }

            // Rute 2: Admin Management (Contoh)
            composable("admin_management") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Halaman Management")
                }
            }

            // Rute 3: Admin Report (Contoh)
            composable("admin_report") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Halaman Report")
                }
            }

            // Rute 4: Admin Profile ⬅️ PERBAIKAN DI SINI
            composable("admin_profile") {
                AdminProfileScreen (
                    paddingValues = paddingValues,
                    adminName = "Admin",
                    adminEmail = "admin@gmail.com",
                    // 3. Ganti navController yang error dengan adminNavController
                    onLogoutClick = { adminNavController.popBackStack() }
                )
            }
        }
    }
}