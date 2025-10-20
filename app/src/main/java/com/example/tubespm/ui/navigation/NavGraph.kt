package com.example.tubespm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tubespm.data.model.sampleQuizQuestions
import com.example.tubespm.ui.screens.*
import com.example.tubespm.ui.screens.activity.ActivityLatihanScreen
import com.example.tubespm.ui.screens.activity.ActivityScreen
import com.example.tubespm.ui.screens.activity.ActivityTryoutScreen
import com.example.tubespm.ui.screens.quiz.QuizMode
import com.example.tubespm.ui.screens.quiz.QuizScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen() }
        composable("exercises") { ExerciseScreen() }
        composable("activity") { ActivityScreen(navController = navController) }
        composable("profile") { ProfileScreen() }
        composable("activity_tryout_list") { ActivityTryoutScreen(navController = navController) }
        composable("activity_latihan_list") { ActivityLatihanScreen(navController = navController) }
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
    }
}
