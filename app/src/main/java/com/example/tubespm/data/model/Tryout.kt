package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Tambahkan default value (misal: "", 0, emptyList()) untuk SEMUA properti

// Menampung kisi-kisi/topik
data class Topic(
    val topicId: String= "",
    val name: String= "",
)

// Menampung subtest (misal: Penalaran Umum) (Punya Data Asli)
data class Subtest(
    val subtestName: String = "",
    val subtestId: String = "",
    val topics: List<Topic> = emptyList(),

    val duration: Int = 0, //menyimpan data asli
    val questionCount: Int = 0 //menyimpan data asli
)

// Section (Menghitung dari Subtest)
data class Section(
    val sectionName: String = "", // <-- Menggantikan 'displayName'
    val sectionId: String = "",   // <-- Menggantikan 'title' (untuk tag "TPS")
    val subtests: List<Subtest> = emptyList() // <-- Menggantikan 'subSections'

    //Ini dihapus
//    val sectionDuration: Int = 0,      // <-- Menggantikan 'totalDuration'
//    val sectionQuestionCount: Int = 0, // <-- Menggantikan 'totalQuestions'
) {
    val sectionDuration: Int
        get() = subtests.sumOf { it.duration }

    val sectionQuestionCount: Int
        get() = subtests.sumOf { it.questionCount }

}

//Tryout (Menghitung dari Section)
data class Tryout(
    @DocumentId // <-- Anotasi ini akan otomatis mengambil ID dokumen
    val id: String = "", // ID unik acak dari Firestore (ZxY...Pq)

    val code: String = "",

    val title: String = "",
    val status: String = "",

    // [BARU] Field Statistik
    val takenCount: Int = 0,      // Jumlah user yang mengambil (klik start)
    val attemptCount: Int = 0,    // Jumlah user yang selesai submit
    val totalScoreSum: Double = 0.0, // (Opsional) Untuk bantu hitung rata-rata
    val averageScore: Double = 0.0,
    val highestScore: Double = 0.0,

    // --- PERUBAHAN DI SINI: Hapus totalDuration & totalQuestionCount dari sini ---
    // (Field ini tidak lagi diambil dari database)
//    val totalDuration: Int = 0,
//    val totalQuestionCount: Int = 0,

    @ServerTimestamp
    val createdAt: Date? = null, // Bisa null jika data lama belum punya field ini

    // List of sections (nama field 'sections' sudah cocok dengan DB)
    val sections: List<Section> = emptyList(),
    val totalDuration: Int = 0,
    val totalQuestionCount: Int = 0
)
//{
//    // Properti ini akan dihitung otomatis setiap kali data Tryout dimuat.
//    // Firestore akan MENGABAIKAN ini saat mapping (karena bukan di constructor).
//    @get:Exclude
//    val totalDuration: Int
//        get() = sections.sumOf { it.sectionDuration }
//
//    @get:Exclude
//    val totalQuestionCount: Int
//        get() = sections.sumOf { section ->
//            section.subtests.sumOf { it.questionCount }
//        }
//}

