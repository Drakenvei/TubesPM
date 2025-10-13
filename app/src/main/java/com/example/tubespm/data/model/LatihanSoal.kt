package com.example.tubespm.data.model

data class LatihanSoal(
    val id: Int,
    val title: String,
    val subtest: String,
    val questionCount: Int,
    val kisiKisi: List<String>
)

// Data Dummy
fun sampleLatihanList() = listOf(
    LatihanSoal(
        id = 1,
        title = "Latihan Sakti 1 - Penalaran Umum",
        subtest = "Penalaran Umum",
        questionCount = 30,
        kisiKisi = listOf(
            "Analogi verbal",
            "Hubungan sebab-akibat",
            "Silogisme",
            "Penarikan kesimpulan logis",
            "Penalaran pola (gambar & teks)"
        )
    ),
    LatihanSoal(
        id = 2,
        title = "Latihan Sakti 2 - Penalaran Umum",
        subtest = "Penalaran Umum",
        questionCount = 30,
        kisiKisi = listOf("Kisi-kisi untuk latihan 2 Penalaran Umum.")
    ),
    LatihanSoal(
        id = 3,
        title = "Latihan Sakti 1 - Penalaran Matematika",
        subtest = "Penalaran Matematika",
        questionCount = 30,
        kisiKisi = listOf("Kisi-kisi untuk latihan 1 Penalaran Matematika.")
    ),
    LatihanSoal(
        id = 4,
        title = "Latihan Sakti 3 - PPU",
        subtest = "Pengetahuan dan Pemahaman Umum",
        questionCount = 30,
        kisiKisi = listOf("Kisi-kisi untuk latihan 3 PPU.")
    )
)