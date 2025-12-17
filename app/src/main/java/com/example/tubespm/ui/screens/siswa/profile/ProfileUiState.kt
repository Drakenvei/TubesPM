package com.example.tubespm.ui.screens.siswa.profile

import com.example.tubespm.ui.screens.admin.management.PaketSoal

data class ProfileUiState(
    //Status UI
    val isLoading: Boolean = false,
    val error: String? = null,

    //Data Profile (yang bisa jadi berasal dari UserModel):
    val name: String = "",
    val email: String = "",
    val school: String = "",
    val profileImageUrl: String = "",

    // Statistik Aktivitas (Hanya yang SELESAI dikerjakan)
    val tryoutCount: Int = 0,
    val latihanCount: Int = 0,

    // ✅ PERUBAHAN: Variabel baru untuk menampung total paket yang PERNAH DIAMBIL (Status: Baru, Progress, Selesai)
    val totalPaketDiambil: Int = 0
)