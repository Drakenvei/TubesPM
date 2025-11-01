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
    val error: String? = null
)

// Event untuk navigasi (satu kali)
sealed class AuthEvent {
    object NavigateToMain : AuthEvent()
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

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            sendToast("Enter email and password")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWithEmailAndPassword(email.trim(), pass).await()
                _uiState.update { it.copy(isLoading = false) }
                sendToast("Signed in")
                _authEvent.send(AuthEvent.NavigateToMain)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                sendToast("Login failed: ${e.localizedMessage}")
            }
        }
    }

    fun register(name: String, email: String, pass: String, confirm: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            sendToast("Fill all fields")
            return
        }
        if (pass != confirm) {
            sendToast("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val uid = authResult.user?.uid

                if (uid != null) {
                    val userMap = mapOf("name" to name.trim(), "email" to email.trim())
                    db.collection("users").document(uid).set(userMap).await()

                    _uiState.update { it.copy(isLoading = false) }
                    sendToast("Account created")
                    _authEvent.send(AuthEvent.NavigateToMain)
                } else {
                    throw Exception("User UID is missing")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                sendToast("Register failed: ${e.localizedMessage}")
            }
        }
    }

    private fun sendToast(message: String) {
        viewModelScope.launch {
            _authEvent.send(AuthEvent.ShowToast(message))
        }
    }
}