package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class LatihanSoal(
    @DocumentId
    val id: String = "",

    val code: String = "",
    val codeLower: String = "", // <--- TAMBAH INI
    val title: String = "",
    val titleLower: String = "", // <--- TAMBAH INI
    val subtest: String = "",
    val subtestId: String = "",
    val questionCount: Int = 0,
    val status: String = "inactive", // Default 'inactive'
    val takenCount: Int = 0,

    @ServerTimestamp
    val createdAt: Date? = null,

    val topics: List<Topic> = emptyList()
)