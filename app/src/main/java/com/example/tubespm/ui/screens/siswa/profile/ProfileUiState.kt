package com.example.tubespm.ui.screens.siswa.profile

data class ProfileUiState(
    //Status UI
    val isLoading: Boolean = false,
    val error: String? = null,

    //Data Profile (yang bisa jadi berasal dari UserModel):
    val name: String = "",
    val email: String = "",
    val school: String = "",
    val profileImageUrl: String = "",
    val tryoutCount: Int = 0,
    val latihanCount: Int = 0
)