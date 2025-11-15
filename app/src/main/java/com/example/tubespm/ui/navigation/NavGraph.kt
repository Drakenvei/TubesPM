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

import com.example.tubespm.data.model.sampleQuizQuestions
import com.example.tubespm.ui.screens.*
import com.example.tubespm.ui.screens.admin.homepage.AdminHomeScreen
import com.example.tubespm.ui.screens.admin.profile.AdminProfileScreen
import com.example.tubespm.ui.screens.pembahasan.PembahasanScreen
import com.example.tubespm.ui.screens.siswa.activity.ActivityLatihanScreen
import com.example.tubespm.ui.screens.siswa.activity.ActivityScreen
import com.example.tubespm.ui.screens.siswa.activity.ActivityTryoutScreen
import com.example.tubespm.ui.screens.siswa.exercises.ExerciseScreen
import com.example.tubespm.ui.screens.siswa.homepage.HomeScreen
import com.example.tubespm.ui.screens.siswa.notification.NotificationScreen
import com.example.tubespm.ui.screens.siswa.profile.EditProfileScreen
import com.example.tubespm.ui.screens.siswa.profile.ProfileScreen
import com.example.tubespm.ui.screens.siswa.quiz.QuizMode
import com.example.tubespm.ui.screens.siswa.quiz.QuizScreen

// =======================================================
// 0. LEGACY: kalau ada kode lama yang masih pakai NavGraph()
//    Ini cuma wrapper ke StudentNavGraph dengan padding 0dp
// =======================================================
@Composable
fun NavGraph(navController: NavHostController) {
    StudentNavGraph(
        navController = navController,
        paddingValues = PaddingValues(0.dp)
    )
}

// =======================================================
// 1. NAVIGATION GRAPH SISWA (ISI SCREEN SAJA)
//    ROUTE SISWA TIDAK DIUBAH: "home", "exercises", "activity", "profile"
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
        composable("exercises") { ExerciseScreen() }
        composable("activity") { ActivityScreen(navController = navController) }
        composable("profile") {
            ProfileScreen(
                onEditClick = { navController.navigate("edit_profile") },
                onSettingsClick = {}
            )
        }

        composable("notification") {
            NotificationScreen(onBackClick = {navController.popBackStack()})
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

        // --- Quiz screens (tanpa parameter dulu) ---
        composable(
            route = "tryout_quiz/{activityId}",
            arguments = listOf(navArgument("activityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
            QuizScreen(
                navController = navController,
                activityId = activityId // <-- Kirim ke QuizScreen
            )
        }
//        composable("latihan_quiz") {
//            QuizScreen(
//                navController = navController,
//                questions = sampleQuizQuestions(),
//                mode = QuizMode.LATIHAN,
//                onSessionFinished = { navController.popBackStack() }
//            )
//        }

        // --- Pembahasan ---
        composable(
            route = "pembahasan/{activityId}",
            arguments = listOf(navArgument("activityId") {type = NavType.StringType })
        ) {
            // Kita tidak perlu meneruskan activityId,
            // PembahasanViewModel akan mengambilnya dari SavedStateHandle
            PembahasanScreen(
                navController = navController,
            )
        }
    }
}

// =======================================================
// 2. NAVIGATION GRAPH ADMIN (ISI SCREEN SAJA)
// =======================================================
@Composable
fun AdminNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = "admin_home",
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        composable("admin_home") {
            AdminHomeScreen(paddingValues = paddingValues)
        }

        composable("admin_management") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Halaman Management Admin")
            }
        }

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

        composable("admin_profile") {
            AdminProfileScreen(
                paddingValues = paddingValues,
                onLogoutClick = { navController.popBackStack() }
            )
        }
    }
}

// =======================================================
// 3. ROOT SISWA: SCAFFOLD + BOTTOM NAVBAR + StudentNavGraph
// =======================================================
@Composable
fun StudentRoot() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Bottom navbar untuk siswa (file BottomNavBar.kt)
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        StudentNavGraph(
            navController = navController,
            paddingValues = paddingValues
        )
    }
}

// =======================================================
// 4. ROOT ADMIN: SCAFFOLD + BOTTOM NAVBAR ADMIN + AdminNavGraph
// =======================================================
@Composable
fun AdminRoot() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Ganti BottomNavbarAdmin dengan nama composable navbar admin punyamu
            BottomNavbarAdmin(navController = navController)
        }
    ) { paddingValues ->
        AdminNavGraph(
            navController = navController,
            paddingValues = paddingValues
        )
    }
}

// =======================================================
// 5. ROUTER UTAMA BERDASARKAN ROLE USER
//    Panggil ini dari MainActivity -> setContent { RoleRouter(role) }
// =======================================================
@Composable
fun RoleRouter(userRole: String) {
    when (userRole) {
        "student" -> StudentRoot()
        "admin" -> AdminRoot()
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Silakan Login")
            }
        }
    }
}
