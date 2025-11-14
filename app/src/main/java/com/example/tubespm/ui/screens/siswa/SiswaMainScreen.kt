package com.example.tubespm.ui.screens.siswa

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.navigation.BottomNavBar
import com.example.tubespm.ui.navigation.StudentNavGraph

@Composable
fun SiswaMainScreen() {
    val navController = rememberNavController()

    // Dapatkan rute yang sedang aktif
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // TAMPILKAN BOTTOM BAR HANYA DI 4 ROUTE UTAMA
    val showBottomBar = when (currentRoute) {
        "home",
        "exercises",
        "activity",
        "profile" -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        StudentNavGraph(
            navController = navController,
            paddingValues = innerPadding
        )
    }
}
