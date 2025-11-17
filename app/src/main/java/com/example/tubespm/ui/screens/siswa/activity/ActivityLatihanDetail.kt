package com.example.tubespm.ui.screens.siswa.activity

import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.UserActivity

/**
 * Data class gabungan yang menyimpan data aktivitas user (seperti status)
 * DAN data metadata latihan (seperti subtest, questionCount).
 */
data class ActivityLatihanDetail(
    val userActivity: UserActivity,
    val latihanSoal: LatihanSoal
)