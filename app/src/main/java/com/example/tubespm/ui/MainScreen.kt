package com.example.tubespm.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.navigation.BottomNavBar
import com.example.tubespm.ui.navigation.NavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()

//    Dapatkan rute (sebagai String) yang sedang aktif
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = when (currentRoute) {
        "home",
        "exercises",
        "activity",
        "profile" -> true
        else -> false
    }

    Scaffold(
        // 3. Jadikan bottomBar kondisional berdasarkan variabel showBottomBar
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            NavGraph(navController = navController)
        }
    }
}
