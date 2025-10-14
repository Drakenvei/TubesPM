package com.example.tubespm.data.model

// File ini berisi model data spesifik untuk merepresentasikan status
// Tryout di halaman Aktivitas (Dalam Proses dan Selesai).

// ===========================================
// 🧩 Data Models for In-Progress & Completed States Tryout
// ===========================================
data class InProgressSectionState(
    val title: String,
    val progress: Int,
    val totalQuestions: Int,
    val remainingTime: String?, // Null jika belum dimulai
    val isLocked: Boolean
)

data class TryoutInProgress(
    val id: Int,
    val title: String,
    val sections: List<InProgressSectionState>
)

data class CompletedSectionState(
    val title: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val completionTime: String
)

data class TryoutCompleted(
    val id: Int,
    val title: String,
    val completionDate: String,
    val sections: List<CompletedSectionState>
)

// ===========================================
// 📋 Dummy Data for New States
// ===========================================
fun sampleInProgressList() = listOf(
    TryoutInProgress(
        id = 1,
        title = "Paket Tryout Sakti (TO-001)",
        sections = listOf(
            InProgressSectionState("TPS", 40, 80, "0:42:34", isLocked = false),
            InProgressSectionState("Literasi", 0, 80, null, isLocked = true)
        )
    )
)

fun sampleCompletedList() = listOf(
    TryoutCompleted(
        id = 1,
        title = "Paket Tryout Sakti (TO-001)",
        completionDate = "25/09/2025",
        sections = listOf(
            CompletedSectionState("TPS", 71, 80, "01:26:34"),
            CompletedSectionState("Literasi", 68, 80, "1:28:53")
        )
    )
)