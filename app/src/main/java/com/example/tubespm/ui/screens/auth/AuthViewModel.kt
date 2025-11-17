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
    val loginError: String? = null, // Pesan error khusus untuk login

    // State untuk Register
    val regName: String = "",
    val regEmail: String = "",
    val regPass: String = "",
    val regConfirm: String = "",
    val regError: String? = null // Pesan error khusus untuk register
)

// Event untuk navigasi (satu kali)
sealed class AuthEvent {
    data class NavigateWithRole(val role: String) : AuthEvent()
    data class ShowToast(val message: String) : AuthEvent()
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

    // Hapus parameter, karena kita akan ambil data dari state
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
                // Logika Firebase tetap sama
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = authResult.user?.uid ?: throw Exception("User UID is missing")

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

    // Hapus parameter, kita ambil data dari state
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
                val uid = authResult.user?.uid ?: throw Exception("User UID is missing")

                val userMap = mapOf(
                    "name" to name,
                    "email" to email,
                    "role" to "siswa",
                    "school" to "",
                    "profileImageUrl" to "",
                    "tryoutCompleted" to 0,
                    "latihanCompleted" to 0
                )
                db.collection("users").document(uid).set(userMap).await()

                _uiState.update { it.copy(isLoading = false) }
                sendToast("Account created successfully")
                _authEvent.send(AuthEvent.NavigateWithRole("siswa"))

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