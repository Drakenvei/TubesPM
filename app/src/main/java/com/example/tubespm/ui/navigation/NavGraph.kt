package com.example.tubespm.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Import screen Anda
import com.example.tubespm.ui.screens.admin.homepage.AdminHomeScreen
import com.example.tubespm.ui.screens.admin.profile.AdminProfileScreen
import com.example.tubespm.ui.screens.admin.management.ManajemenTryoutScreen
import com.example.tubespm.ui.screens.admin.management.EditQuestionScreen
import com.example.tubespm.ui.screens.pembahasan.PembahasanScreen
import com.example.tubespm.ui.screens.siswa.activity.ActivityLatihanScreen
import com.example.tubespm.ui.screens.siswa.activity.ActivityScreen
import com.example.tubespm.ui.screens.siswa.activity.ActivityTryoutScreen
import com.example.tubespm.ui.screens.siswa.analisis.AnalisisScoreScreen
import com.example.tubespm.ui.screens.siswa.exercises.ExerciseScreen
import com.example.tubespm.ui.screens.siswa.homepage.HomeScreen
import com.example.tubespm.ui.screens.siswa.notification.NotificationScreen
import com.example.tubespm.ui.screens.siswa.profile.EditProfileScreen
import com.example.tubespm.ui.screens.siswa.profile.ProfileScreen
import com.example.tubespm.ui.screens.siswa.quiz.QuizScreen

// =======================================================
// 0. LEGACY
// =======================================================
@Composable
fun NavGraph(navController: NavHostController) {
    StudentNavGraph(
        navController = navController,
        paddingValues = PaddingValues(0.dp)
    )
}

// =======================================================
// 1. NAVIGATION GRAPH SISWA
// =======================================================
@Composable
fun StudentNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // --- Bottom bar screens ---
        composable("home") { HomeScreen(navController = navController) }
        composable(
            "exercises?type={type}&query={query}",
            arguments = listOf(
                navArgument("type"){
                    type = NavType.StringType
                    defaultValue = "tryout" // Default buka Tryout
                },
                navArgument("query"){
                    type = NavType.StringType
                    defaultValue = "" // Default tidak ada pencaria
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "tryout"
            val query = backStackEntry.arguments?.getString("query") ?: ""

            ExerciseScreen(
                initialTab = if (type == "latihan") 1 else 0, // 0=Tryout, 1=Latihan
                initialSearchQuery = query
            )
        }

        composable("activity") { ActivityScreen(navController = navController) }
        composable("profile") {
            ProfileScreen(
                onEditClick = { navController.navigate("edit_profile") },
                onSettingsClick = {}
            )
        }

        composable("notification") {
            NotificationScreen(onBackClick = { navController.popBackStack() })
        }

        // --- Profile / Edit ---
        composable("edit_profile") {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- Activity list ---
        composable("activity_tryout_list") {
            ActivityTryoutScreen(navController = navController)
        }
        composable("activity_latihan_list") {
            ActivityLatihanScreen(navController = navController)
        }
        composable(
            route = "analisis/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) {
            AnalisisScoreScreen(navController = navController)
        }

        // --- Quiz screens ---
        composable(
            route = "tryout_quiz/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
            QuizScreen(
                navController = navController,
                activityId = activityId
            )
        }
        composable(
            route = "latihan_quiz/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
            QuizScreen(
                navController = navController,
                activityId = activityId
            )
        }

        // --- Pembahasan ---
        composable(
            route = "pembahasan/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) {
            PembahasanScreen(navController = navController)
        }
    }
}

// =======================================================
// 2. NAVIGATION GRAPH ADMIN
// =======================================================
@Composable
fun AdminNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = "admin_home",
        modifier = Modifier.fillMaxSize()
    ) {

        // ------------ Admin Home ------------
        composable("admin_home") {
            AdminHomeScreen(paddingValues = paddingValues)
        }

        // ------------ Manajemen Tryout ------------
        composable("admin_management") {
            ManajemenTryoutScreen(
                paddingValuesFromNavHost = paddingValues,
                // PERBAIKAN DI SINI:
                // Tangkap 4 parameter yang dikirim dari ManajemenTryoutScreen
                onGoToEditQuestion = { tryoutId, questionId, paketName, questionNumber ->

                    // Gunakan parameter tersebut untuk navigasi
                    navController.navigate("admin_edit_question/$tryoutId/$questionId/$paketName/$questionNumber")
                }
            )
        }

        // ------------ Edit Question (DIPERBAIKI) ------------
        composable(
            // Tambahkan argumen di route
            route = "admin_edit_question/{tryoutId}/{questionId}/{paketName}/{questionNumber}",
            arguments = listOf(
                navArgument("tryoutId") { type = NavType.StringType },
                navArgument("questionId") { type = NavType.StringType },
                navArgument("paketName") { type = NavType.StringType },
                navArgument("questionNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            // Ambil data dari argument
            val tryoutId = backStackEntry.arguments?.getString("tryoutId") ?: ""
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            val paketName = backStackEntry.arguments?.getString("paketName") ?: ""
            val questionNumber = backStackEntry.arguments?.getInt("questionNumber") ?: 1

            // Panggil Screen dengan parameter lengkap
            EditQuestionScreen(
                tryoutId = tryoutId,
                questionId = questionId,
                paketName = paketName,
                questionNumber = questionNumber,
                paddingValuesFromNavHost = paddingValues,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ------------ Admin Report ------------
        composable("admin_report") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Halaman Report Admin")
            }
        }

        // ------------ Admin Profile ------------
        composable("admin_profile") {
            AdminProfileScreen(
                paddingValues = paddingValues,
                onLogoutClick = { navController.popBackStack() }
            )
        }
    }
}

// =======================================================
// 3. ROOT SISWA
// =======================================================
@Composable
fun StudentRoot() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavBar(navController = navController) }) { paddingValues ->
        StudentNavGraph(navController = navController, paddingValues = paddingValues)
    }
}

// =======================================================
// 4. ROOT ADMIN
// =======================================================
@Composable
fun AdminRoot() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavbarAdmin(navController = navController) }) { paddingValues ->
        AdminNavGraph(navController = navController, paddingValues = paddingValues)
    }
}

// =======================================================
// 5. ROUTER UTAMA
// =======================================================
@Composable
fun RoleRouter(userRole: String) {
    when (userRole) {
        "student" -> StudentRoot()
        "admin" -> AdminRoot()
        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Silakan Login") }
    }
}