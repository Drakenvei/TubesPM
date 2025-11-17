package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Tambahkan default value (misal: "", 0, emptyList()) untuk SEMUA properti

// Menampung kisi-kisi/topik
data class Topic(
    val topicId: String= "",
    val name: String= "",
)

// Menampung subtest (misal: Penalaran Umum)
data class Subtest(
    val subtestName: String = "",
    val subtestId: String = "",
    val duration: Int = 0,
    val questionCount: Int = 0,
    val topics: List<Topic> = emptyList()
)

data class Section(
    val sectionName: String = "", // <-- Menggantikan 'displayName'
    val sectionId: String = "",   // <-- Menggantikan 'title' (untuk tag "TPS")
    val sectionDuration: Int = 0,      // <-- Menggantikan 'totalDuration'
    val sectionQuestionCount: Int = 0, // <-- Menggantikan 'totalQuestions'
    val subtests: List<Subtest> = emptyList() // <-- Menggantikan 'subSections'
)

data class Tryout(
    @DocumentId // <-- Anotasi ini akan otomatis mengambil ID dokumen
    val id: String = "", // ID unik acak dari Firestore (ZxY...Pq)

    val code: String = "",

    val title: String = "",
    val status: String = "",

    // Info total (denormalized dari database)
    val totalDuration: Int = 0,
    val totalQuestionCount: Int = 0,

    @ServerTimestamp
    val createdAt: Date? = null, // Bisa null jika data lama belum punya field ini

    // List of sections (nama field 'sections' sudah cocok dengan DB)
    val sections: List<Section> = emptyList()
)

