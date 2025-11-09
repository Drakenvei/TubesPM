package com.example.tubespm.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tubespm.data.model.QuizQuestion
import com.example.tubespm.data.model.sampleQuestionsWithExplanation
import com.example.tubespm.data.model.sampleQuizQuestions
import com.example.tubespm.repository.QuestionRepository
import com.example.tubespm.ui.screens.*
import com.example.tubespm.ui.screens.siswa.activity.ActivityLatihanScreen
import com.example.tubespm.ui.screens.siswa.activity.ActivityScreen
import com.example.tubespm.ui.screens.siswa.activity.ActivityTryoutScreen
import com.example.tubespm.ui.screens.siswa.exercises.ExerciseScreen
import com.example.tubespm.ui.screens.pembahasan.PembahasanScreen
import com.example.tubespm.ui.screens.siswa.quiz.QuizMode
import com.example.tubespm.ui.screens.siswa.quiz.QuizScreen
import com.example.tubespm.ui.screens.siswa.homepage.HomeScreen
import com.example.tubespm.ui.screens.siswa.profile.EditProfileScreen
import com.example.tubespm.ui.screens.siswa.profile.ProfileScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        // Main bottom navigation screens
        composable("home") { HomeScreen() }
        composable("exercises") { ExerciseScreen() }
        composable("activity") { ActivityScreen(navController = navController) }
        composable("profile") {
            ProfileScreen(
                onEditClick = {
                    navController.navigate("edit_profile")
                },
                onSettingsClick = {}
            )
        }

        composable("edit_profile") {
            EditProfileScreen (
                onBackClick = {navController.popBackStack()}
            )
        }

        // Activity list screens
        composable("activity_tryout_list") {
            ActivityTryoutScreen(navController = navController)
        }
        composable("activity_latihan_list") {
            ActivityLatihanScreen(navController = navController)
        }

        composable("tryout_quiz") {
            QuizScreen(
                navController = navController,
                questions = sampleQuizQuestions(),
                mode = QuizMode.TRYOUT,
                onSessionFinished = {
                    navController.popBackStack()
                }
            )
        }
        composable("latihan_quiz") {
            QuizScreen(
                navController = navController,
                questions = sampleQuizQuestions(),
                mode = QuizMode.LATIHAN,
                onSessionFinished = {
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // TRYOUT QUIZ - WITH PARAMETERS
        // ==========================================
//        composable(
//            route = "tryout_quiz/{tryoutId}/{sectionId}",
//            arguments = listOf(
//                navArgument("tryoutId") { type = NavType.IntType },
//                navArgument("sectionId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val tryoutId = backStackEntry.arguments?.getInt("tryoutId") ?: 0
//            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
//
//            val questionRepository = remember { QuestionRepository() }
//            var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
//            var isLoading by remember { mutableStateOf(true) }
//            var errorMessage by remember { mutableStateOf<String?>(null) }
//
//            // Load questions from Firebase
//            LaunchedEffect(tryoutId, sectionId) {
//                isLoading = true
//                errorMessage = null
//                try {
//                    questions = questionRepository.getQuestionsByTryoutSection(tryoutId, sectionId)
//                } catch (e: Exception) {
//                    errorMessage = "Gagal memuat soal: ${e.message}"
//                } finally {
//                    isLoading = false
//                }
//            }
//
//            // Show loading, error, or quiz
//            when {
//                isLoading -> {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//                errorMessage != null -> {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Text(errorMessage ?: "Terjadi kesalahan")
//                            Spacer(Modifier.height(16.dp))
//                            Button(onClick = { navController.popBackStack() }) {
//                                Text("Kembali")
//                            }
//                        }
//                    }
//                }
//                questions.isEmpty() -> {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Text("Belum ada soal untuk tryout ini")
//                            Spacer(Modifier.height(16.dp))
//                            Button(onClick = { navController.popBackStack() }) {
//                                Text("Kembali")
//                            }
//                        }
//                    }
//                }
//                else -> {
//                    QuizScreen(
//                        navController = navController,
//                        questions = questions,
//                        mode = QuizMode.TRYOUT,
//                        onSessionFinished = { userAnswers ->
//                            // TODO: Save results to Firebase
//                            navController.popBackStack()
//                        }
//                    )
//                }
//            }
//        }

        // ==========================================
        // LATIHAN QUIZ - WITH PARAMETERS
        // ==========================================
//        composable(
//            route = "latihan_quiz/{latihanId}",
//            arguments = listOf(
//                navArgument("latihanId") { type = NavType.IntType }
//            )
//        ) { backStackEntry ->
//            val latihanId = backStackEntry.arguments?.getInt("latihanId") ?: 0
//
//            val questionRepository = remember { QuestionRepository() }
//            var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
//            var isLoading by remember { mutableStateOf(true) }
//            var errorMessage by remember { mutableStateOf<String?>(null) }
//
//            // Load questions from Firebase
//            LaunchedEffect(latihanId) {
//                isLoading = true
//                errorMessage = null
//                try {
//                    questions = questionRepository.getQuestionsByLatihanId(latihanId)
//                } catch (e: Exception) {
//                    errorMessage = "Gagal memuat soal: ${e.message}"
//                } finally {
//                    isLoading = false
//                }
//            }
//
//            // Show loading, error, or quiz
//            when {
//                isLoading -> {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//                errorMessage != null -> {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Text(errorMessage ?: "Terjadi kesalahan")
//                            Spacer(Modifier.height(16.dp))
//                            Button(onClick = { navController.popBackStack() }) {
//                                Text("Kembali")
//                            }
//                        }
//                    }
//                }
//                questions.isEmpty() -> {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Text("Belum ada soal untuk latihan ini")
//                            Spacer(Modifier.height(16.dp))
//                            Button(onClick = { navController.popBackStack() }) {
//                                Text("Kembali")
//                            }
//                        }
//                    }
//                }
//                else -> {
//                    QuizScreen(
//                        navController = navController,
//                        questions = questions,
//                        mode = QuizMode.LATIHAN,
//                        onSessionFinished = { userAnswers ->
//                            // TODO: Save results to Firebase
//                            navController.popBackStack()
//                        }
//                    )
//                }
//            }
//        }

        // ==========================================
        // PEMBAHASAN SCREEN
        // ==========================================
        composable("pembahasan_latihan") {
            PembahasanScreen(
                navController = navController,
                questions = sampleQuestionsWithExplanation()
            )
        }
    }
}