package com.example.tubespm.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.navigation.BottomNavbarAdmin
import com.example.tubespm.ui.screens.admin.homepage.AdminHomeScreen
import com.example.tubespm.ui.screens.admin.profile.AdminProfileScreen
import com.example.tubespm.ui.screens.admin.management.ManajemenTryoutScreen

@Composable
fun AdminMainScreen(
    rootNavController: NavHostController
) {
    val adminNavController = rememberNavController() // NavController untuk navigasi internal Admin

    Scaffold(
        bottomBar = { BottomNavbarAdmin(navController = adminNavController) }
    ) { paddingValues -> // paddingValues ini datang dari Scaffold, untuk BottomBar

        NavHost(
            navController = adminNavController,
            startDestination = "admin_home",
            // PERBAIKAN: Hapus modifier = Modifier.fillMaxSize() dari NavHost.
            // Biarkan padding diatur oleh setiap Composable di dalamnya.
        ) {
            // Rute 1: Admin Home
            composable("admin_home") {
                AdminHomeScreen(paddingValues = paddingValues)
            }

            // Rute 2: Admin Management (SUDAH DIPERBAIKI)
            composable("admin_management") {
                ManajemenTryoutScreen(paddingValuesFromNavHost = paddingValues)
            }

            // Rute 3: Admin Report (Contoh)
            composable("admin_report") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues), // Terapkan padding di sini
                    contentAlignment = Alignment.Center
                ) {
                    Text("Halaman Report")
                }
            }

            // Rute 4: Admin Profile
            composable("admin_profile") {
                AdminProfileScreen (
                    paddingValues = paddingValues,
                    // Tambahkan parameter adminName dan adminEmail jika diperlukan di AdminProfileScreen
                    // adminName = "Admin",
                    // adminEmail = "admin@gmail.com",
                    onLogoutClick = {
                        adminNavController.popBackStack()
                    }
                )
            }
        }
    }
}