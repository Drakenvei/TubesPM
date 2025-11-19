package com.example.tubespm.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Event satu kali (One-time event) untuk navigasi
sealed class AdminMainEvent {
    object NavigateToLogin : AdminMainEvent()
}

class AdminMainViewModel : ViewModel() {

    // Channel untuk mengirim event navigasi ke UI
    private val _eventChannel = Channel<AdminMainEvent>()
    val eventChannel = _eventChannel.receiveAsFlow()

    /**
     * Fungsi Logout:
     * Menghapus sesi Firebase Auth dan mengirim sinyal ke UI untuk pindah layar.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // 1. Proses Logout dari Firebase
                Firebase.auth.signOut()

                // 2. Kirim event ke UI bahwa logout berhasil
                _eventChannel.send(AdminMainEvent.NavigateToLogin)
            } catch (e: Exception) {
                // Handle error jika perlu (opsional)
                e.printStackTrace()
                // Tetap force logout di UI jika error network
                _eventChannel.send(AdminMainEvent.NavigateToLogin)
            }
        }
    }
}