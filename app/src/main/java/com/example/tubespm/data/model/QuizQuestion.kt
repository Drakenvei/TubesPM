package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentId

// Ini merepresentasikan dokumen di /tryouts/{id}/questions/{id}
data class QuizQuestion(
    @DocumentId
    val id: String = "",
    val questionNumber: Int = 0,
    val subtestId: String = "", // "pu", "pk", dll.
    val topicId: String = "",
    val questionText: String = "",
    val questionImage: String? = null,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val discussion: String = ""
)