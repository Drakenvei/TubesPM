package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentId

data class LatihanSoal(
    @DocumentId
    val id: String = "", // <-- ID unik Firestore (misal: latihan_aljabar_01)

    val code: String = "", // <-- Kode buatan admin (misal: LAT-ALG-01)
    val title: String = "",
    val subtest: String = "",
    val questionCount: Int = 0,
    val status: String = "",
    val topics: List<Topic> = emptyList() // data class Topic diambil dari model Tryout.kt
)
