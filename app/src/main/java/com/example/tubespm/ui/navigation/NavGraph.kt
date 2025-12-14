package com.example.tubespm.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
import com.example.tubespm.ui.screens.admin.management.CreateQuestionScreen
import com.example.tubespm.ui.screens.admin.management.CreateLatihanSoalScreen
import com.example.tubespm.ui.screens.admin.management.CreateTryoutScreen
import com.example.tubespm.ui.screens.admin.management.EditLatihanSoalScreen
import com.example.tubespm.ui.screens.admin.management.ListSoalScreen
import com.example.tubespm.ui.screens.pembahasan.DiskusiAiScreen
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
import com.example.tubespm.ui.screens.siswa.settings.EditPasswordScreen
import com.example.tubespm.ui.screens.siswa.settings.SettingScreen

// =======================================================
// 0. LEGACY
// =======================================================
//@Composable
//fun NavGraph(navController: NavHostController) {
//    StudentNavGraph(
//        navController = navController,
//        paddingValues = PaddingValues(0.dp)
//    )
//}

// =======================================================
// 1. NAVIGATION GRAPH SISWA
// =======================================================
@Composable
fun StudentNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    rootNavController: NavController
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
                onSettingsClick = { navController.navigate("settings")}
            )
        }

        composable("notification") {
            NotificationScreen(onBackClick = { navController.popBackStack() })
        }

        // --- Profile / Edit ---
        composable("edit_profile") {
            EditProfileScreen(
                navController = navController,
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

        // --- DISKUSI AI ---
        composable(
            // Route harus mencakup kedua argumen
            route = "diskusi_ai/{activityId}/{questionIndex}",
            arguments = listOf(
                navArgument("activityId") { type = NavType.StringType },
                navArgument("questionIndex") { type = NavType.IntType }
            )
        ) {
            // Memanggil fungsi Composable baru yang sudah kita buat
            DiskusiAiScreen(navController = navController)
        }

        // --- Setting ---
        composable("settings") {
            SettingScreen(
                navController = navController,
                onLogout = {
                    rootNavController.navigate("auth") {
                        popUpTo("siswa_main") {inclusive = true}
                        launchSingleTop = true
                    }
                }
            )
        }
        composable ("change_password") {
            EditPasswordScreen(navController = navController)
        }
    }
}

// =======================================================
// 2. NAVIGATION GRAPH ADMIN
// =======================================================
@Composable
fun AdminNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    rootNavController: NavController
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
                onGoToEditQuestion = { type, parentId, questionId, paketName, questionNumber ->
                    when {
                        questionId == "edit_nama" && type == "latihan_soal" -> {
                            navController.navigate("admin_edit_latihan/$parentId")
                        }
                        questionId.startsWith("list") -> {
                            // HERE IS THE LOGIC:
                            // If EditManagement sends questionId as "list_soal|subtest_123", split it
                            val parts = questionId.split("|")
                            val actualSubtestId = if (parts.size > 1) parts[1] else null

                            // DEBUG LOG (Cek di Logcat)
                            android.util.Log.d("AdminNav", "Raw: $questionId, Parsed ID: $actualSubtestId")

                            // Kita manfaatkan parameter 'questionNumber' (Int) untuk membawa 'targetCount'
                            // karena saat membuka LIST, nomor soal tidak dibutuhkan.
                            val targetCount = questionNumber

                            val route = if (actualSubtestId != null && actualSubtestId.isNotBlank()) {
                                // Pastikan formatnya benar
                                "admin_list_soal/$type/$parentId/$paketName?subtestId=$actualSubtestId&targetCount=$targetCount"
                            } else {
                                "admin_list_soal/$type/$parentId/$paketName?targetCount=$targetCount"
                            }
                            navController.navigate(route)
                        }
                        else -> {
                            navController.navigate("admin_edit_question/$type/$parentId/$questionId/$paketName/$questionNumber")
                        }
                    }
                },
                onNavigateToCreateTryout = {
                    navController.navigate("admin_create_tryout")
                },
                onNavigateToCreateLatihan = {
                    navController.navigate("admin_create_latihan")
                }
            )
        }

        // ------------ Edit Question (DIPERBAIKI) ------------
        composable(
            // Tambahkan argumen di route
            route = "admin_edit_question/{type}/{tryoutId}/{questionId}/{paketName}/{questionNumber}?displayNumber={displayNumber}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("tryoutId") { type = NavType.StringType },
                navArgument("questionId") { type = NavType.StringType },
                navArgument("paketName") { type = NavType.StringType },
                navArgument("questionNumber") { type = NavType.IntType },
                navArgument("displayNumber") {
                    type = NavType.IntType
                    defaultValue = 0 // Default 0 jika tidak dikirim
                }
            )
        ) { backStackEntry ->
            // Ambil data dari argument
            val type = backStackEntry.arguments?.getString("type") ?: "tryout"
            val tryoutId = backStackEntry.arguments?.getString("tryoutId") ?: ""
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            val paketName = backStackEntry.arguments?.getString("paketName") ?: ""
            val questionNumber = backStackEntry.arguments?.getInt("questionNumber") ?: 1
            val displayNumberArg = backStackEntry.arguments?.getInt("displayNumber") ?: 0
            // Logika Fallback: Jika displayNumber 0 (tidak dikirim), pakai questionNumber lama
            val finalDisplayNumber = if (displayNumberArg > 0) displayNumberArg else questionNumber

            // Panggil Screen dengan parameter lengkap
            EditQuestionScreen(
                tryoutId = tryoutId,
                questionId = questionId,
                paketName = paketName,
                questionNumber = questionNumber, // Tetap kirim ID Database ke ViewModel
                displayNumber = finalDisplayNumber, // [BARU] Kirim ID Visual ke UI
                paddingValuesFromNavHost = paddingValues,
                onBackClick = { navController.popBackStack() },
                type = type
            )
        }

        // ------------ Create Question ------------
        composable(
            route = "admin_create_question/{type}/{parentId}/{paketName}/{questionNumber}?subtestId={subtestId}&displayNumber={displayNumber}&targetCount={targetCount}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("parentId") { type = NavType.StringType },
                navArgument("paketName") { type = NavType.StringType },
                navArgument("questionNumber") { type = NavType.IntType },
                navArgument("subtestId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("displayNumber") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("targetCount") {
                    type = NavType.IntType
                    defaultValue = 0 // 0 = Unlimited
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val parentId = backStackEntry.arguments?.getString("parentId") ?: ""
            val paketName = backStackEntry.arguments?.getString("paketName") ?: ""
            val questionNumber = backStackEntry.arguments?.getInt("questionNumber") ?: 1
            val subtestId = backStackEntry.arguments?.getString("subtestId")?.takeIf { it.isNotEmpty() }
            val displayNumberArg = backStackEntry.arguments?.getInt("displayNumber") ?: 0
            val finalDisplayNumber = if (displayNumberArg > 0) displayNumberArg else questionNumber
            val targetCount = backStackEntry.arguments?.getInt("targetCount") ?: 0

            CreateQuestionScreen(
                parentId = parentId,
                type = type,
                paketName = paketName,
                questionNumber = questionNumber, // ID Database (misal 5)
                displayNumber = finalDisplayNumber, // ID Visual (misal 4)
                subtestId = subtestId,
                targetCount = targetCount,
                paddingValuesFromNavHost = paddingValues,
                onBackClick = { navController.popBackStack() },
                onQuestionCreated = {
                    // Kembali ke halaman sebelumnya setelah berhasil
                    navController.popBackStack()
                }
            )
        }

        // ------------ Create Latihan Soal ------------
        composable("admin_create_latihan") {
            CreateLatihanSoalScreen(
                paddingValuesFromNavHost = paddingValues,
                onBackClick = { navController.popBackStack() },
                onLatihanCreated = { latihanId, subtestId ->
                    // Navigasi ke Create Question Screen
                    navController.navigate("admin_create_question/latihan_soal/$latihanId/Latihan Soal Baru/1?subtestId=$subtestId") {
                        popUpTo("admin_management") { inclusive = false }
                    }
                }
            )
        }

        // ------------ Create Tryout ------------
        composable("admin_create_tryout") {
            CreateTryoutScreen(
                paddingValuesFromNavHost = paddingValues,
                onBackClick = { navController.popBackStack() },
                onTryoutCreated = { tryoutId ->
                    // Navigasi ke Edit Management untuk menambah section
                    // Atau bisa langsung ke Create Section Screen
                    navController.navigate("admin_management") {
                        popUpTo("admin_management") { inclusive = false }
                        // Setelah tryout dibuat, admin bisa klik settings untuk tambah section
                    }
                }
            )
        }

        // ------------ List Soal ------------
        composable(
            route = "admin_list_soal/{type}/{parentId}/{paketName}?subtestId={subtestId}&targetCount={targetCount}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("parentId") { type = NavType.StringType },
                navArgument("paketName") { type = NavType.StringType },
                // CHANGE 2: Add argument definition for subtestId (optional)
                navArgument("subtestId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("targetCount") {
                    type = NavType.IntType
                    defaultValue = 0 // 0 artinya tidak dibatasi (default)
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val parentId = backStackEntry.arguments?.getString("parentId") ?: ""
            val paketName = backStackEntry.arguments?.getString("paketName") ?: ""
            // CHANGE 3: Capture the subtestId
            val subtestId = backStackEntry.arguments?.getString("subtestId")

            val targetCount = backStackEntry.arguments?.getInt("targetCount") ?: 0

            // Extract section name dari paketName jika format "Paket - Section"
            val sectionName = if (paketName.contains(" - ")) {
                paketName.split(" - ").getOrNull(1)
            } else {
                null
            }

            val actualSectionId: String? = if (sectionName != null && type == "tryout") {
                sectionName // Temporary: gunakan section name sebagai identifier
            } else {
                null
            }

            ListSoalScreen(
                parentId = parentId,
                type = type,
                sectionName = null,
                subtestId = subtestId,
                sectionId = null,
                paketName = paketName,
                targetQuestionCount = targetCount,
                onBackClick = { navController.popBackStack() },
                onEditQuestion = { t, pId, qId, pName, qNum, dNum ->
                    navController.navigate("admin_edit_question/$t/$pId/$qId/$pName/$qNum?displayNumber=$dNum")
                },
                onAddQuestion = { t, pId, pName, qNum, sId, dNum, tCount ->
                    // CHANGE 4: Construct navigation with subtestId parameter
//                    val subtestParam = if (sId != null) "?subtestId=$sId" else ""
                    // Tambahkan query param &displayNumber=$dNum
                    // Perhatikan penggunaan '&' karena subtestParam mungkin kosong atau sudah pakai '?'
                    // Logika aman: url?param1&param2

                    // Cara paling aman menyusun string URL:
                    val baseUrl = "admin_create_question/$t/$pId/$pName/$qNum"
                    val params = mutableListOf<String>()
                    if (sId != null) params.add("subtestId=$sId")
                    params.add("displayNumber=$dNum")
                    params.add("targetCount=$tCount")

                    val fullRoute = if (params.isEmpty()) baseUrl else "$baseUrl?${params.joinToString("&")}"

                    navController.navigate(fullRoute)
                }
            )
        }

        // ------------ Edit Latihan Soal ------------
        composable(
            route = "admin_edit_latihan/{latihanId}",
            arguments = listOf(
                navArgument("latihanId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val latihanId = backStackEntry.arguments?.getString("latihanId") ?: ""

            EditLatihanSoalScreen(
                latihanId = latihanId,
                paddingValuesFromNavHost = paddingValues,
                onBackClick = { navController.popBackStack() },
                onLatihanUpdated = {
                    navController.popBackStack()
                },
                onGoToListSoal = { type, parentId, paketName, subtestId ->
                    navController.navigate("admin_list_soal/$type/$parentId/$paketName?subtestId={subtestId}")
                }
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
                onLogoutClick = {
                    // Panggil fungsi logout ViewModel di dalam Screen,
                    // lalu navigasi menggunakan rootNavController

                    // Navigasi paksa ke Auth/Login
                    rootNavController.navigate("auth") {
                        popUpTo("admin_main") { inclusive = true }
                        launchSingleTop = true
                    }
                }
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
        StudentNavGraph(navController = navController, paddingValues = paddingValues, rootNavController = navController)
    }
}

// =======================================================
// 4. ROOT ADMIN
// =======================================================
@Composable
fun AdminRoot() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavbarAdmin(navController = navController) }) { paddingValues ->
        AdminNavGraph(navController = navController, paddingValues = paddingValues, rootNavController = navController)
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
//fun RoleRouter(userRole: String, rootNavController: NavController) { // Butuh rootNavController
//    when (userRole) {
//        "student" -> StudentRoot() // StudentRoot biasanya buat controller sendiri, tapi cek StudentNavGraph butuh root tidak? Ya butuh.
//        "admin" -> AdminRoot(rootNavController = rootNavController) // Oper di sini
//        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Silakan Login") }
//    }
//}