package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Merepresentasikan data MENTAH seorang user seperti yang ada di Firestore.
 * Berbeda dari UiState, class ini tidak tahu tentang isLoading atau error.
 */
data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val school: String = "",
    val role: String = "",
    val profileImageUrl: String = "",
    val tryoutCompleted: Int = 0,
    val latihanCompleted: Int = 0,
)

/**
 * Fungsi helper untuk mengubah DocumentSnapshot dari Firestore
 * menjadi data model UserModel kita.
 */
fun DocumentSnapshot.toUserModel(uid: String): UserModel{
    return UserModel(
        uid = uid,
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        school = getString("school") ?: "",
        role = getString("role") ?: "siswa",
        profileImageUrl = getString("profileImageUrl") ?: "",
        tryoutCompleted = getLong("tryoutCompleted")?.toInt() ?: 0,
        latihanCompleted = getLong("latihanCompleted")?.toInt() ?: 0,
    )
}