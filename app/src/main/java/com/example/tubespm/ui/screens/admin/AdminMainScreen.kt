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
import com.example.tubespm.ui.screens.admin.management.ManajemenTryoutScreen
import com.example.tubespm.ui.screens.admin.management.EditQuestionScreen
import com.example.tubespm.ui.screens.admin.profile.AdminProfileScreen

@Composable
fun AdminMainScreen(
    rootNavController: NavHostController
) {
    // NavController khusus untuk tab Admin
    val adminNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavbarAdmin(navController = adminNavController) }
    ) { paddingValues ->

        NavHost(
            navController = adminNavController,
            startDestination = "admin_home",
        ) {
            // Rute 1: Admin Home
            composable("admin_home") {
                AdminHomeScreen(
                    paddingValues = paddingValues
                )
            }

            // Rute 2: Admin Management
            composable("admin_management") {
                ManajemenTryoutScreen(
                    paddingValuesFromNavHost = paddingValues,
                    onGoToEditQuestion = {
                        // pindah ke halaman edit soal
                        adminNavController.navigate("admin_edit_question")
                    }
                )
            }

            // Rute 3: Admin Report (contoh placeholder)
            composable("admin_report") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Halaman Report")
                }
            }

            // Rute 4: Admin Profile
            composable("admin_profile") {
                AdminProfileScreen(
                    paddingValues = paddingValues,
                    onLogoutClick = {
                        // keluar dari stack admin, kembali ke root
                        adminNavController.popBackStack()
                        // kalau mau kembali ke login di rootNavController bisa tambahkan:
                        // rootNavController.navigate("login") { popUpTo(0) }
                    }
                )
            }

            // Rute 5: Edit Question (halaman edit soal tryout)
            composable("admin_edit_question") {
                EditQuestionScreen(
                    paketName = "TO-001 (Penalaran Umum)", // sementara dummy
                    questionNumber = 1,
                    paddingValuesFromNavHost = paddingValues,
                    onBackClick = { adminNavController.popBackStack() }
                )
            }
        }
    }
}
