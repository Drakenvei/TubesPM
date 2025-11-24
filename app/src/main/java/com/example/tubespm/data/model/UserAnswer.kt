package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentId

// Ini merepresentasikan dokumen di /user_activities/{id}/answers/{id}
data class UserAnswer(
    @DocumentId
    val questionId: String = "", // <-- ID-nya adalah ID Soal
    val questionNumber: Int = 0,
    val userAnswer: String = "", // Pilihan user (mis: "A", "B")
    val isCorrect: Boolean = false
)