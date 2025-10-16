package com.example.tubespm.data.model

data class LatihanInProgress (
    val id: Int,
    val title: String,
    val subtest: String,
    val progress: Int,
    val totalQuestions: Int,
)

data class LatihanCompleted (
    val id: Int,
    val title: String,
    val subtest: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val completionDate: String
)

fun sampleLatihanInProgressList() = listOf(
    LatihanInProgress(
        id = 1,
        title = "Latihan Sakti 1 - Penalaran Umum",
        subtest = "Penalaran Umum",
        progress = 20,
        totalQuestions = 30,
    )
)

fun sampleLatihanCompletedList() = listOf(
    LatihanCompleted(
        id = 1,
        title = "Latihan Sakti 1 - Penalaran Umum",
        subtest = "Penalaran Umum",
        correctAnswers = 24,
        totalQuestions = 30,
        completionDate = "14/10/2025"
    ),
    LatihanCompleted(
        id = 2,
        title = "Latihan Sakti 2 - PMM",
        subtest = "Pengetahuan dan Pemahaman Umum",
        correctAnswers = 26,
        totalQuestions = 30,
        completionDate = "8/10/2025"
    )

)