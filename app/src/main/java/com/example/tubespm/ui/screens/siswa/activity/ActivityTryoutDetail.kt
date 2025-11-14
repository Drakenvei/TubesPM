package com.example.tubespm.ui.screens.siswa.activity

import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.UserActivity

/**
 * Data class gabungan yang menyimpan data aktivitas user (seperti status)
 * DAN data metadata tryout (seperti sections, title, dll).
 */
data class ActivityTryoutDetail(
    val userActivity: UserActivity,
    val tryout: Tryout
)