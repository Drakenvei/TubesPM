package com.example.tubespm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tubespm.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen() }
        composable("exercises") { ExerciseScreen() }
        composable("report") { /* TODO: ReportScreen() */ }
        composable("profile") { /* TODO: ProfileScreen() */ }
    }
}
