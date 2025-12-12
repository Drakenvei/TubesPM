package com.example.tubespm.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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
    val isResettingPassword: Boolean = false
)

// Event untuk navigasi (satu kali)
sealed class AuthEvent {
    data class NavigateWithRole(val role: String) : AuthEvent()
    data class ShowToast(val message: String) : AuthEvent()
    object RegistrationSuccess : AuthEvent()
    // Event baru: pemicu verifikasi email saat login
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
    }

    fun onRegConfirmChanged(confirm: String) {
        _uiState.update { it.copy(regConfirm = confirm, regError = null) }
    }

    // Fungsi Pengubah State untuk Reset Password
    fun onResetEmailChanged(email: String) {
        _uiState.update { it.copy(resetEmail = email) }
    }

    // Fungsi Reset Password
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            sendToast("Please enter your email address.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResettingPassword = true) }
            try {
                auth.sendPasswordResetEmail(email).await()
                sendToast("Password reset link sent to $email. Please check your inbox.")
            } catch (e: Exception) {
                val error = e.localizedMessage ?: "Failed to send reset email."
                sendToast(error)
            } finally {
                _uiState.update { it.copy(isResettingPassword = false, resetEmail = "") }
            }
        }
    }

    // FUNGSI GABUNGAN: Kirim Ulang Email Verifikasi
    fun resendVerificationEmail() {
        val user = auth.currentUser
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Coba kirim menggunakan current user (kasus setelah Register)
                if (user != null && user.email != null) {
                    user.sendEmailVerification().await()
                    sendToast("Verification email sent again to ${user.email}")
                }
                // 2. Jika tidak ada current user (kasus setelah Gagal Login, user sudah di-sign out)
                else if (state.loginEmail.isNotBlank() && state.loginPass.isNotBlank()) {
                    // Coba sign in ulang secara diam-diam hanya untuk mendapatkan user object
                    val authResult = auth.signInWithEmailAndPassword(state.loginEmail, state.loginPass).await()
                    val tempUser = authResult.user

                    if (tempUser?.isEmailVerified == false) {
                        tempUser.sendEmailVerification().await()
                        sendToast("Verification email sent again to ${tempUser.email}")
                        auth.signOut() // Sign out lagi setelah kirim
                    } else {
                        // User sudah terverifikasi, kirim toast dan kembali ke login
                        sendToast("Email is already verified. Please sign in now.")
                        auth.signOut()
                    }
                } else {
                    sendToast("Error: User information missing for resend. Please try signing in again.")
                }
            } catch (e: Exception) {
                val error = e.localizedMessage ?: "Failed to resend verification email."
                sendToast(error)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // MODIFIKASI FUNGSI LOGIN
    fun login() {
        val state = _uiState.value
        val email = state.loginEmail.trim()
        val pass = state.loginPass

        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(loginError = "Email and Password must be filled") }
            sendToast("Email and Password must be filled")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }
            try {
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val user = authResult.user

                // CHECK VERIFIKASI EMAIL
                if (user?.isEmailVerified == false) {
                    // Jika belum terverifikasi:
                    auth.signOut()
                    _uiState.update { it.copy(isLoading = false) }
                    // Kirim event untuk memicu UI verifikasi
                    _authEvent.send(AuthEvent.VerifyEmailPrompt(email))
                    return@launch // Hentikan fungsi login di sini
                }

                // Lanjutkan ke Firestore dan navigasi jika terverifikasi
                val uid = user?.uid ?: throw Exception("User UID is missing")

                // Ambil role dari Firestore dan navigasi
                val userDoc = db.collection("users").document(uid).get().await()
                val role = userDoc.getString("role") ?: "siswa"

                _uiState.update { it.copy(isLoading = false) }
                sendToast("Signed in as $role")
                _authEvent.send(AuthEvent.NavigateWithRole(role))

            } catch (e: Exception) {
                val error = e.localizedMessage ?: "Login failed"
                _uiState.update { it.copy(isLoading = false, loginError = error) }
                sendToast(error)
            }
        }
    }

    fun register() {
        //ambil data dari state
        val state = _uiState.value
        val name = state.regName.trim()
        val email = state.regEmail.trim()
        val pass = state.regPass
        val confirm = state.regConfirm

        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(regError = "All fields must be filled") }
            sendToast("All fields must be filled")
            return
        }
        if (pass != confirm) {
            _uiState.update { it.copy(regError = "Password is not the same") }
            sendToast("Password is not the same")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, regError = null) }
            try {
                val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val user = authResult.user
                val uid = user?.uid ?: throw Exception("User UID is missing")

                // KIRIM EMAIL VERIFIKASI
                user.sendEmailVerification().await()

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

                // Kirim event sukses registrasi
                _authEvent.send(AuthEvent.RegistrationSuccess)

            } catch (e: Exception) {
                val error = e.localizedMessage ?: "Register failed"
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