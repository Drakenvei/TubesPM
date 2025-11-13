package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentId

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

    // List of sections (nama field 'sections' sudah cocok dengan DB)
    val sections: List<Section> = emptyList()
)

//
// ===========================================
// 📋 Dummy Data
// ===========================================
//fun sampleTryoutList() = listOf(
//    Tryout(
//        id = 1,
//        title = "Paket Tryout Sakti (TO-001)",
//        code = "TO-001",
//        sections = listOf(
//            TryoutSection(
//                title = "TPS",
//                displayName = "Tryout TPS",
//                totalQuestions = 80,
//                totalDuration = 90,
//                subSections = listOf(
//                    SubSectionDetail("Penalaran Umum", 30, 30, listOf(
//                        "Analogi verbal", "Hubungan sebab-akibat", "Silogisme", "Penarikan kesimpulan logis", "Penalaran pola (gambar & teks)"
//                    )),
//                    SubSectionDetail("Pemahaman Membaca dan Menulis", 20, 15, listOf(
//                        "Menemukan ide pokok", "Menentukan kalimat utama", "Menyimpulkan bacaan", "Hubungan antarparagraf", "Struktur teks (eksposisi, narasi, argumentasi)", "Perbaikan Kalimat/Tata Bahasa"
//                    )),
//                    SubSectionDetail("Pengetahuan dan Pemahaman Umum", 20, 25, listOf(
//                        "Ide Pokok Bacaan", "Kata Bentuk Kata", "Kesesuaian Wacana", "Hubungan Antar Paragraf", "Sinonim"
//                    )),
//                    SubSectionDetail("Pengetahuan Kuantitatif", 20, 20, listOf(
//                        "Bilangan", "Aljabar dan Fungsi", "Geometri", "Statistika dan Peluang"
//                    ))
//                )
//            ),
//            TryoutSection(
//                title = "Literasi",
//                displayName = "Tryout Literasi",
//                totalQuestions = 80,
//                totalDuration = 105,
//                subSections = listOf(
//                    SubSectionDetail("Literasi Indonesia", 30, 42, listOf(
//                        "Makna Kata", "Prosa Baru", "Teks Eksemplum", "Simpulan Teks", "Informasi isi teks (tersurat/tersirat)", "Gagasan Utama Bacaan / Inti Teks"
//                    )),
//                    SubSectionDetail("Literasi Bahasa Inggris", 20, 20, listOf(
//                        "Purpose of the Text / Passage", "Synonym / Antonym of a Word", "Writer's Attitude / Tone towards the Text / Passage", "General/Main Idea/Topic"
//                    )),
//                    SubSectionDetail("Penalaran Matematika", 20, 42, listOf(
//                        "Bilangan", "Himpunan", "Pola Bilangan", "Aljabar dan Fungsi", "Aritmetika Sosial", "Persamaan Garis Lurus"
//                    ))
//                )
//            )
//        )
//    ),
//    Tryout(
//        id = 2,
//        title = "Paket Tryout Sakti (TO-002)",
//        code = "TO-002",
//        sections = listOf(
//            TryoutSection("TPS", "Tryout TPS", 80, 90, emptyList()),
//            TryoutSection("Literasi", "Tryout Literasi", 80, 90, emptyList())
//        )
//    ),
//    Tryout(
//        id = 3,
//        title = "Paket Tryout Sakti (TO-003)",
//        code = "TO-003",
//        sections = listOf(
//            TryoutSection("TPS", "Tryout TPS", 80, 90, emptyList()),
//            TryoutSection("Literasi", "Tryout Literasi", 80, 90, emptyList())
//        )
//    )
//)
//
