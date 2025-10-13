package com.example.tubespm.data.model

data class  SubSectionDetail(
    val name: String,
    val questionCount: Int,
    val duration: Int,
    val kisiKisi: List<String>
)

data class TryoutSection(
    val title: String,
    val displayName: String,
    val totalQuestions: Int,
    val totalDuration: Int,
    val subSections: List<SubSectionDetail>
)

data class Tryout(
    val id: Int,
    val title: String,
    val code: String,
    val sections: List<TryoutSection>
) {
    val totalQuestions: Int
        get() = sections.sumOf { it.totalQuestions }
    val totalDuration: Int
        get() = sections.sumOf { it.totalDuration }
}

// ===========================================
// 📋 Dummy Data
// ===========================================
fun sampleTryoutList() = listOf(
    Tryout(
        id = 1,
        title = "Paket Tryout Sakti (TO-001)",
        code = "TO-001",
        sections = listOf(
            TryoutSection(
                title = "TPS",
                displayName = "Tryout TPS",
                totalQuestions = 80,
                totalDuration = 90,
                subSections = listOf(
                    SubSectionDetail("Penalaran Umum", 30, 30, listOf(
                        "Analogi verbal", "Hubungan sebab-akibat", "Silogisme", "Penarikan kesimpulan logis", "Penalaran pola (gambar & teks)"
                    )),
                    SubSectionDetail("Pemahaman Membaca dan Menulis", 20, 15, listOf(
                        "Menemukan ide pokok", "Menentukan kalimat utama", "Menyimpulkan bacaan", "Hubungan antarparagraf", "Struktur teks (eksposisi, narasi, argumentasi)", "Perbaikan Kalimat/Tata Bahasa"
                    )),
                    SubSectionDetail("Pengetahuan dan Pemahaman Umum", 20, 25, listOf(
                        "Ide Pokok Bacaan", "Kata Bentuk Kata", "Kesesuaian Wacana", "Hubungan Antar Paragraf", "Sinonim"
                    )),
                    SubSectionDetail("Pengetahuan Kuantitatif", 20, 20, listOf(
                        "Bilangan", "Aljabar dan Fungsi", "Geometri", "Statistika dan Peluang"
                    ))
                )
            ),
            TryoutSection(
                title = "Literasi",
                displayName = "Tryout Literasi",
                totalQuestions = 80,
                totalDuration = 105,
                subSections = listOf(
                    SubSectionDetail("Literasi Indonesia", 30, 42, listOf(
                        "Makna Kata", "Prosa Baru", "Teks Eksemplum", "Simpulan Teks", "Informasi isi teks (tersurat/tersirat)", "Gagasan Utama Bacaan / Inti Teks"
                    )),
                    SubSectionDetail("Literasi Bahasa Inggris", 20, 20, listOf(
                        "Purpose of the Text / Passage", "Synonym / Antonym of a Word", "Writer's Attitude / Tone towards the Text / Passage", "General/Main Idea/Topic"
                    )),
                    SubSectionDetail("Penalaran Matematika", 20, 42, listOf(
                        "Bilangan", "Himpunan", "Pola Bilangan", "Aljabar dan Fungsi", "Aritmetika Sosial", "Persamaan Garis Lurus"
                    ))
                )
            )
        )
    ),
    Tryout(
        id = 2,
        title = "Paket Tryout Sakti (TO-002)",
        code = "TO-002",
        sections = listOf(
            TryoutSection("TPS", "Tryout TPS", 80, 90, emptyList()),
            TryoutSection("Literasi", "Tryout Literasi", 80, 90, emptyList())
        )
    ),
    Tryout(
        id = 3,
        title = "Paket Tryout Sakti (TO-003)",
        code = "TO-003",
        sections = listOf(
            TryoutSection("TPS", "Tryout TPS", 80, 90, emptyList()),
            TryoutSection("Literasi", "Tryout Literasi", 80, 90, emptyList())
        )
    )
)