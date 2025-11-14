package com.example.tubespm.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class AdminNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun BottomNavbarAdmin(navController: NavHostController) {
    val items = listOf(
        AdminNavItem("admin_home", "Home", Icons.Default.Home),
        AdminNavItem("admin_management", "Management", Icons.Default.ManageAccounts),
        AdminNavItem("admin_report", "Report", Icons.Default.ListAlt),
        AdminNavItem("admin_profile", "Profile", Icons.Default.Person)
    )

    // Dapatkan rute yang sedang aktif
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    // Navigasi ke rute baru hanya jika itu bukan rute yang sedang aktif
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Hindari menumpuk layar Home
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Hindari membuat instance destinasi yang sama berkali-kali
                            launchSingleTop = true
                            // Pulihkan status ketika re-seleksi
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = {
                    Text(text = item.label, fontSize = 11.sp)
                }
            )
        }
    }
}