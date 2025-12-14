package com.example.tubespm.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// State untuk UI (loading, error)
data class AuthUiState(
    val isLoading: Boolean = false,

    // State untuk Login
    val loginEmail: String = "",
    val loginPass: String = "",
    val loginError: String? = null,

    // State untuk Register
    val regName: String = "",
    val regEmail: String = "",
    val regPass: String = "",
    val regConfirm: String = "",
    val regError: String? = null,

    // State untuk Forgot Password
    val resetEmail: String = "",
    val isResettingPassword: Boolean = false,

    // Cooldown untuk Kirim Ulang Email (dalam detik)
    val resendCooldown: Int = 0
)

// Event untuk navigasi (satu kali)
sealed class AuthEvent {
    data class NavigateWithRole(val role: String) : AuthEvent()
    data class ShowToast(val message: String) : AuthEvent()
    object RegistrationSuccess : AuthEvent()
    data class VerifyEmailPrompt(val email: String) : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _authEvent = Channel<AuthEvent>()
    val authEvent = _authEvent.receiveAsFlow()

    // Variable internal untuk melacak waktu terakhir kirim (ms)
    private var lastResendTime: Long = 0
    private val COOLDOWN_SECONDS = 60L // ✅ PERUBAHAN: 30L -> 60L

    init {
        // Mulai timer hitung mundur saat ViewModel dibuat
        startResendTimer()
    }

    // FUNGSI BARU: Logika Cooldown Timer
    private fun startResendTimer() {
        viewModelScope.launch {
            while (true) {
                val timeElapsed = System.currentTimeMillis() - lastResendTime
                val timeLeft = COOLDOWN_SECONDS - (timeElapsed / 1000)

                if (timeLeft > 0) {
                    _uiState.update { it.copy(resendCooldown = timeLeft.toInt()) }
                } else {
                    _uiState.update { it.copy(resendCooldown = 0) }
                }
                delay(1000) // Update setiap 1 detik
            }
        }
    }


    fun onLoginEmailChanged(email: String) {
        _uiState.update { it.copy(loginEmail = email, loginError = null) }
    }

    fun onLoginPassChanged(pass: String) {
        _uiState.update { it.copy(loginPass = pass, loginError = null) }
    }

    fun onRegNameChanged(name: String) {
        _uiState.update { it.copy(regName = name, regError = null) }
    }

    fun onRegEmailChanged(email: String) {
        _uiState.update { it.copy(regEmail = email, regError = null) }
    }

    fun onRegPassChanged(pass: String) {
        _uiState.update { it.copy(regPass = pass, regError = null) }

        if (pass.isNotEmpty()) {
            val validationError = isPasswordValid(pass)
            if (validationError != null) {
                _uiState.update { it.copy(regError = validationError) }
            }
        }
    }

    fun onRegConfirmChanged(confirm: String) {
        _uiState.update { it.copy(regConfirm = confirm, regError = null) }
    }

    fun onResetEmailChanged(email: String) {
        _uiState.update { it.copy(resetEmail = email) }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            sendToast("Masukkan alamat email Anda.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResettingPassword = true) }
            try {
                auth.sendPasswordResetEmail(email).await()
                sendToast("Tautan atur ulang kata sandi telah dikirim ke $email. Silakan cek kotak masuk Anda.")
            } catch (e: Exception) {
                val error = e.localizedMessage ?: "Gagal mengirim email atur ulang."
                sendToast(error)
            } finally {
                _uiState.update { it.copy(isResettingPassword = false, resetEmail = "") }
            }
        }
    }

    // FUNGSI Kirim Ulang Email Verifikasi
    fun resendVerificationEmail() {
        if (_uiState.value.resendCooldown > 0) {
            sendToast("Harap tunggu ${_uiState.value.resendCooldown} detik sebelum mencoba lagi.")
            return
        }

        val user = auth.currentUser
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {

                // Panggil fungsi kirim verifikasi dan update timestamp
                fun sendVerificationAndUpdateTime(targetUserEmail: String?) {
                    user?.sendEmailVerification()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            lastResendTime = System.currentTimeMillis() // Update waktu kirim sukses
                            sendToast("Email verifikasi telah dikirim ulang ke $targetUserEmail")
                        } else {
                            val error = task.exception?.localizedMessage ?: "Gagal mengirim ulang email verifikasi."
                            sendToast(error)
                        }
                        _uiState.update { it.copy(isLoading = false) }
                    } ?: run {
                        _uiState.update { it.copy(isLoading = false) }
                        sendToast("Error: Objek pengguna tidak valid.")
                    }
                }

                // 1. Kasus setelah Register (user masih logged in)
                if (user != null && user.email != null) {
                    sendVerificationAndUpdateTime(user.email)
                }
                // 2. Kasus setelah Gagal Login (user sudah di-sign out)
                else if (state.loginEmail.isNotBlank() && state.loginPass.isNotBlank()) {
                    // Coba sign in ulang secara diam-diam
                    val authResult = auth.signInWithEmailAndPassword(state.loginEmail, state.loginPass).await()
                    val tempUser = authResult.user

                    if (tempUser?.isEmailVerified == false) {
                        tempUser.sendEmailVerification().await()
                        lastResendTime = System.currentTimeMillis() // Update waktu kirim sukses
                        sendToast("Email verifikasi telah dikirim ulang ke ${tempUser.email}")
                        auth.signOut() // Sign out lagi setelah kirim
                    } else {
                        sendToast("Email sudah terverifikasi. Silakan masuk sekarang.")
                        auth.signOut()
                    }
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    sendToast("Error: Informasi pengguna tidak ditemukan untuk kirim ulang. Coba masuk kembali.")
                }
            } catch (e: Exception) {
                val error = e.localizedMessage ?: "Gagal mengirim ulang email verifikasi."
                _uiState.update { it.copy(isLoading = false) }
                sendToast(error)
            }
        }
    }

    fun login() {
        val state = _uiState.value
        val email = state.loginEmail.trim()
        val pass = state.loginPass

        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(loginError = "Email dan Kata Sandi harus diisi") }
            sendToast("Email dan Kata Sandi harus diisi")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }
            try {
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val user = authResult.user

                if (user?.isEmailVerified == false) {
                    auth.signOut()
                    _uiState.update { it.copy(isLoading = false) }
                    _authEvent.send(AuthEvent.VerifyEmailPrompt(email))
                    return@launch
                }

                val uid = user?.uid ?: throw Exception("User UID hilang")

                val userDoc = db.collection("users").document(uid).get().await()
                val role = userDoc.getString("role") ?: "siswa"

                _uiState.update { it.copy(isLoading = false) }
                sendToast("Berhasil masuk sebagai $role")
                _authEvent.send(AuthEvent.NavigateWithRole(role))

            } catch (e: Exception) {
                val error = e.localizedMessage ?: "Gagal masuk"
                _uiState.update { it.copy(isLoading = false, loginError = error) }
                sendToast(error)
            }
        }
    }

    private fun isPasswordValid(password: String): String? {
        if (password.length < 8) {
            return "Kata sandi harus minimal 8 karakter."
        }
        if (!password.contains(Regex("[A-Z]"))) {
            return "Kata sandi harus mengandung setidaknya satu huruf besar."
        }
        if (!password.contains(Regex("[0-9]"))) {
            return "Kata sandi harus mengandung setidaknya satu angka (0-9)."
        }
        return null
    }

    // MODIFIKASI FUNGSI REGISTER: Tambahkan lastResendTime = System.currentTimeMillis()
    fun register() {
        val state = _uiState.value
        val name = state.regName.trim()
        val email = state.regEmail.trim()
        val pass = state.regPass
        val confirm = state.regConfirm

        if (name.isBlank() || email.isBlank() || pass.isBlank() || confirm.isBlank()) {
            val error = "Semua kolom harus diisi"
            _uiState.update { it.copy(regError = error) }
            sendToast(error)
            return
        }

        if (pass != confirm) {
            val error = "Konfirmasi Kata Sandi tidak sama"
            _uiState.update { it.copy(regError = error) }
            sendToast(error)
            return
        }

        val passwordValidationError = isPasswordValid(pass)
        if (passwordValidationError != null) {
            _uiState.update { it.copy(regError = passwordValidationError) }
            sendToast(passwordValidationError)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, regError = null) }
            try {
                val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val user = authResult.user
                val uid = user?.uid ?: throw Exception("User UID hilang")

                user.sendEmailVerification().await()

                // AKTIFKAN COOLDOWN SEGERA SETELAH PENGIRIMAN EMAIL SUKSES
                lastResendTime = System.currentTimeMillis()

                val userMap = mapOf(
                    "name" to name,
                    "email" to email,
                    "role" to "siswa",
                    "school" to "",
                    "profile_picture" to "",
                    "tryoutCompleted" to 0,
                    "latihanCompleted" to 0
                )
                db.collection("users").document(uid).set(userMap).await()

                _uiState.update { it.copy(isLoading = false) }

                _authEvent.send(AuthEvent.RegistrationSuccess)

            } catch (e: Exception) {
                val error = e.localizedMessage ?: "Gagal mendaftar"
                _uiState.update { it.copy(isLoading = false, regError = error) }
                sendToast(error)
            }
        }
    }

    private fun sendToast(message: String) {
        viewModelScope.launch {
            _authEvent.send(AuthEvent.ShowToast(message))
        }
    }
}