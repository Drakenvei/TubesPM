package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserActivity(
    @DocumentId
    val id: String = "",

    val userId: String = "",
    val type: String = "", // "tryout" atau "latihan_soal"

    val activityRefId: String = "", // ID ke koleksi 'tryouts' atau 'latihan_soal'
    val activityTitle: String = "", // Judul tryout/latihan (denormalized)

    val status: String = "", // "not_started", "in_progress", "completed"
    val score: Int = 0,
    val correctCount: Int = 0,

    val answeredQuestionCount: Int = 0,

    @ServerTimestamp
    val startedAt: Date? = null,

    val deadline: Date? = null // Akan menyimpan Timestamp kapan kuis berakhir
)