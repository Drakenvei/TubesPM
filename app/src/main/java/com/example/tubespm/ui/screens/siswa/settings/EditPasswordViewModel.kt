package com.example.tubespm.ui.screens.siswa.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditPasswordEvent {
    object Success : EditPasswordEvent()
    data class Error(val message: String) : EditPasswordEvent()
}

@HiltViewModel
class EditPasswordViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _eventFlow = Channel<EditPasswordEvent>()
    val eventFlow = _eventFlow.receiveAsFlow()

    fun changePassword(oldPass: String, newPass: String, confirmPass: String) {
        if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            sendEvent(EditPasswordEvent.Error("Semua kolom harus diisi"))
            return
        }

        if (newPass != confirmPass) {
            sendEvent(EditPasswordEvent.Error("Konfirmasi password tidak cocok"))
            return
        }

        if (newPass.length < 6) {
            sendEvent(EditPasswordEvent.Error("Password baru minimal 6 karakter"))
            return
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    // 1. Buat kredensial dari email user & password LAMA
                    val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)

                    // 2. Re-autentikasi (Wajib untuk ganti password)
                    // Kita gunakan tasks.await() wrapper atau callback standar
                    user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // 3. Jika password lama benar, update password BARU
                            user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                                _isLoading.value = false
                                if (updateTask.isSuccessful) {
                                    sendEvent(EditPasswordEvent.Success)
                                } else {
                                    sendEvent(EditPasswordEvent.Error(updateTask.exception?.localizedMessage ?: "Gagal update"))
                                }
                            }
                        } else {
                            _isLoading.value = false
                            sendEvent(EditPasswordEvent.Error("Password lama salah"))
                        }
                    }
                } catch (e: Exception) {
                    _isLoading.value = false
                    sendEvent(EditPasswordEvent.Error(e.localizedMessage ?: "Terjadi kesalahan"))
                }
            }
        }
    }

    private fun sendEvent(event: EditPasswordEvent) {
        viewModelScope.launch { _eventFlow.send(event) }
    }
}