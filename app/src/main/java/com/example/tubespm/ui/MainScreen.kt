package com.example.tubespm.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.navigation.BottomNavBar
import com.example.tubespm.ui.navigation.NavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Surface (
            modifier = Modifier.padding(innerPadding)
        ) {
            NavGraph(navController = navController)
        }
    }
}
