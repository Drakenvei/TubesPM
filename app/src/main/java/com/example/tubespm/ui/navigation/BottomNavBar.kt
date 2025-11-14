package com.example.tubespm.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(navController: NavHostController) {
    // ROUTE DISAMAKAN DENGAN StudentNavGraph: "home", "exercises", "activity", "profile"
    val items = listOf(
        BottomNavItem(
            label = "Home",
            icon = Icons.Default.Home,
            route = "home"
        ),
        BottomNavItem(
            label = "Exercises",
            icon = Icons.Default.List,
            route = "exercises"
        ),
        BottomNavItem(
            label = "Activity",
            icon = Icons.Default.MenuBook,
            route = "activity"
        ),
        BottomNavItem(
            label = "Profile",
            icon = Icons.Default.Person,
            route = "profile"
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFE0E0E0),
        tonalElevation = 4.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
            )
        }
    }
}
