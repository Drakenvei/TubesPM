package com.example.tubespm.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val school: String = "",
    val role: String = "",

    val profilePicture: String = "",

    val tryoutCompleted: Int = 0,
    val latihanCompleted: Int = 0,

    // ✅ PERBAIKAN: Properti baru untuk Total Paket yang Pernah Diambil
    val totalPaketTaken: Int = 0
)

fun DocumentSnapshot.toUserModel(uid: String): UserModel{
    return UserModel(
        uid = uid,
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        school = getString("school") ?: "",
        role = getString("role") ?: "siswa",

        // Sesuai dengan nama field di Firestore: 'profile_picture'
        profilePicture = getString("profile_picture") ?: "",

        // Mengambil data Selesai dari Firestore /users/{uid}
        tryoutCompleted = getLong("tryoutCompleted")?.toInt() ?: 0,
        latihanCompleted = getLong("latihanCompleted")?.toInt() ?: 0,

        // Dibiarkan 0, karena nilai ini akan ditimpa/ditambahkan di Repository Impl
        totalPaketTaken = 0
    )
}