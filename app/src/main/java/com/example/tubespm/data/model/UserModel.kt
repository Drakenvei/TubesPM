package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val school: String = "",
    val role: String = "",

    // ✅ UBAH DARI 'profileImageUrl' MENJADI 'profilePicture'
    val profilePicture: String = "",

    val tryoutCompleted: Int = 0,
    val latihanCompleted: Int = 0,
)

fun DocumentSnapshot.toUserModel(uid: String): UserModel{
    return UserModel(
        uid = uid,
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        school = getString("school") ?: "",
        role = getString("role") ?: "siswa",

        // ✅ PASTIKAN KEY NYA "profile_picture" (Sesuai Firestore)
        // DAN VARIABELNYA 'profilePicture'
        profilePicture = getString("profile_picture") ?: "",

        tryoutCompleted = getLong("tryoutCompleted")?.toInt() ?: 0,
        latihanCompleted = getLong("latihanCompleted")?.toInt() ?: 0,
    )
}