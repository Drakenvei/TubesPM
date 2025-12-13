package com.example.tubespm.ui.screens.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tubespm.ui.navigation.BottomNavbarAdmin
import com.example.tubespm.ui.screens.admin.homepage.AdminHomeScreen
import com.example.tubespm.ui.screens.admin.management.CreateLatihanSoalScreen
import com.example.tubespm.ui.screens.admin.management.CreateQuestionScreen
import com.example.tubespm.ui.screens.admin.management.CreateTryoutScreen
import com.example.tubespm.ui.screens.admin.management.EditLatihanSoalScreen
import com.example.tubespm.ui.screens.admin.management.EditQuestionScreen
import com.example.tubespm.ui.screens.admin.management.ListSoalScreen
import com.example.tubespm.ui.screens.admin.management.ManajemenTryoutScreen
import com.example.tubespm.ui.screens.admin.profile.AdminProfileScreen

@Composable
fun AdminMainScreen(
    rootNavController: NavHostController,
    // Inject ViewModel (bisa pakai hiltViewModel() jika sudah setup Hilt, atau viewModel() biasa)
    viewModel: AdminMainViewModel = viewModel()
) {
    // NavController khusus untuk tab Admin (Bottom Navigation)
    val adminNavController = rememberNavController()

    // -------------------------------------------------------
    // OBSERVASI EVENT LOGOUT
    // -------------------------------------------------------
    // Kita mendengarkan event dari ViewModel. Jika event "NavigateToLogin" muncul,
    // kita kembalikan user ke layar Login (root).
    LaunchedEffect(Unit) {
        viewModel.eventChannel.collect { event ->
            when (event) {
                is AdminMainEvent.NavigateToLogin -> {
                    // Hapus semua backstack admin dan kembali ke login
                    rootNavController.navigate("auth") { // Pastikan rute login Anda bernama "login"
                        popUpTo("admin_main") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

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
                    // Navigasi ke Edit Question (menggunakan adminNavController)
                    onGoToEditQuestion = { type, parentId, questionId, paketName, questionNumber ->
                        when {
                            questionId == "edit_nama" && type == "latihan_soal" -> {
                                adminNavController.navigate("admin_edit_latihan/$parentId")
                            }
                            questionId.startsWith("list") -> {
                                // LOGIKA BARU: Parse string "list_soal|subtest_123"
                                val parts = questionId.split("|")
                                // parts[0] = "list_soal"
                                // parts[1] = "subtest_123" (jika ada)
                                val actualSubtestId = if (parts.size > 1) parts[1] else null

                                val targetCount = questionNumber

                                // Bangun Route String
                                val route = if (actualSubtestId != null) {
                                    "admin_list_soal/$type/$parentId/$paketName?subtestId=$actualSubtestId&targetCount=$targetCount"
                                } else {
                                    "admin_list_soal/$type/$parentId/$paketName?targetCount=$targetCount"
                                }
                                adminNavController.navigate(route)
                            }
                            else -> {
                                // Navigate to edit question
                                adminNavController.navigate("admin_edit_question/$type/$parentId/$questionId/$paketName/$questionNumber")
                            }
                        }
                    },
                    onNavigateToCreateTryout = {
                        adminNavController.navigate("admin_create_tryout")
                    },
                    onNavigateToCreateLatihan = {
                        adminNavController.navigate("admin_create_latihan")
                    }
                )
            }

            // Rute 3: Admin Report
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
                        viewModel.logout()
                    }
                )
            }

            // Rute 5: Edit Question
            composable(
                route = "admin_edit_question/{type}/{tryoutId}/{questionId}/{paketName}/{questionNumber}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("tryoutId") { type = NavType.StringType },
                    navArgument("questionId") { type = NavType.StringType },
                    navArgument("paketName") { type = NavType.StringType },
                    navArgument("questionNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "tryout"
                val tryoutId = backStackEntry.arguments?.getString("tryoutId") ?: ""
                val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
                val paketName = backStackEntry.arguments?.getString("paketName") ?: ""
                val questionNumber = backStackEntry.arguments?.getInt("questionNumber") ?: 1

                EditQuestionScreen(
                    tryoutId = tryoutId,
                    questionId = questionId,
                    paketName = paketName,
                    questionNumber = questionNumber,
                    paddingValuesFromNavHost = paddingValues,
                    onBackClick = { adminNavController.popBackStack() },
                    type = type
                )
            }

            // Rute 6: Create Tryout
            composable("admin_create_tryout") {
                CreateTryoutScreen(
                    paddingValuesFromNavHost = paddingValues,
                    onBackClick = { adminNavController.popBackStack() },
                    onTryoutCreated = { tryoutId ->
                        // Navigasi kembali ke management setelah tryout dibuat
                        adminNavController.navigate("admin_management") {
                            popUpTo("admin_management") { inclusive = false }
                        }
                    }
                )
            }

            // Rute 7: Create Latihan Soal
            composable("admin_create_latihan") {
                CreateLatihanSoalScreen(
                    paddingValuesFromNavHost = paddingValues,
                    onBackClick = { adminNavController.popBackStack() },
                    onLatihanCreated = { latihanId, subtestId ->
                        // Navigasi ke Create Question Screen
                        adminNavController.navigate("admin_create_question/latihan_soal/$latihanId/Latihan Soal Baru/1?subtestId=$subtestId") {
                            popUpTo("admin_management") { inclusive = false }
                        }
                    }
                )
            }

            // Rute 8: Create Question
            composable(
                route = "admin_create_question/{type}/{parentId}/{paketName}/{questionNumber}?subtestId={subtestId}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("parentId") { type = NavType.StringType },
                    navArgument("paketName") { type = NavType.StringType },
                    navArgument("questionNumber") { type = NavType.IntType },
                    navArgument("subtestId") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: ""
                val parentId = backStackEntry.arguments?.getString("parentId") ?: ""
                val paketName = backStackEntry.arguments?.getString("paketName") ?: ""
                val questionNumber = backStackEntry.arguments?.getInt("questionNumber") ?: 1
                val subtestId = backStackEntry.arguments?.getString("subtestId")?.takeIf { it.isNotEmpty() }

                CreateQuestionScreen(
                    parentId = parentId,
                    type = type,
                    paketName = paketName,
                    questionNumber = questionNumber,
                    subtestId = subtestId,
                    paddingValuesFromNavHost = paddingValues,
                    onBackClick = { adminNavController.popBackStack() },
                    onQuestionCreated = {
                        // Kembali ke halaman sebelumnya setelah soal berhasil dibuat
                        adminNavController.popBackStack()
                    }
                )
            }

            // Rute 9: List Soal
            composable(
                route = "admin_list_soal/{type}/{parentId}/{paketName}?subtestId={subtestId}&targetCount={targetCount}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("parentId") { type = NavType.StringType },
                    navArgument("paketName") { type = NavType.StringType },
                    navArgument("subtestId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("targetCount") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: ""
                val parentId = backStackEntry.arguments?.getString("parentId") ?: ""
                val paketName = backStackEntry.arguments?.getString("paketName") ?: ""

                val subtestId = backStackEntry.arguments?.getString("subtestId")

                val targetCount = backStackEntry.arguments?.getInt("targetCount") ?: 0

                // Extract section name dari paketName jika format "Paket - Section"
                val sectionName = if (paketName.contains(" - ")) {
                    paketName.split(" - ").getOrNull(1)
                } else {
                    null
                }
                
                // Extract sectionId dari paketName (section name bisa digunakan untuk mencari sectionId)
                // Untuk sekarang, kita load semua soal dan filter di ViewModel berdasarkan section name
                // Atau bisa di-improve dengan pass sectionId sebagai parameter terpisah
//                val actualSectionId: String? = if (sectionName != null && type == "tryout") {
//                    // Akan di-extract di ViewModel berdasarkan section name
//                    sectionName // Temporary: gunakan section name sebagai identifier
//                } else {
//                    null
//                }

                ListSoalScreen(
                    parentId = parentId,
                    type = type,
                    sectionName = sectionName,
                    subtestId = subtestId,
                    sectionId = null,
                    paketName = paketName,
                    targetQuestionCount = targetCount,
                    onBackClick = { adminNavController.popBackStack() },
                    onEditQuestion = { t, pId, qId, pName, qNum ->
                        adminNavController.navigate("admin_edit_question/$t/$pId/$qId/$pName/$qNum")
                    },
                    onAddQuestion = { t, pId, pName, qNum, sId ->
                        val subtestParam = if (sId != null) "?subtestId=$sId" else ""
                        adminNavController.navigate("admin_create_question/$t/$pId/$pName/$qNum$subtestParam")
                    }
                )
            }

            // Rute 10: Edit Latihan Soal
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
                    onBackClick = { adminNavController.popBackStack() },
                    onLatihanUpdated = {
                        adminNavController.popBackStack()
                    },
                    onGoToListSoal = { type, parentId, paketName, subtestId ->
                        // Navigate ke list soal dengan nama latihan yang benar
                        adminNavController.navigate("admin_list_soal/$type/$parentId/$paketName?subtestId={subtestId}&targetCount=0")
                    }
                )
            }
        }
    }
}