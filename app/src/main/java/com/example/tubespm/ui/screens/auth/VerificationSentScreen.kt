package com.example.tubespm.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun VerificationSentScreen(
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    displayEmail: String,
    isLoginAttempt: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val cooldown = uiState.resendCooldown // ✅ Ambil nilai cooldown

    // Tentukan apakah tombol harus dinonaktifkan
    val isResendDisabled = uiState.isLoading || cooldown > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Sent",
                    tint = Color(0xFFFF004E),
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLoginAttempt)
                        "Verify Your Email!"
                    else
                        "Verification Email Sent!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isLoginAttempt)
                        "Akun Anda ($displayEmail) belum terverifikasi. Silakan cek kotak masuk Anda atau klik 'Kirim Ulang Email' di bawah ini untuk mengirim ulang verifikasi."
                    else
                        "Kami telah mengirim tautan verifikasi ke $displayEmail. Silakan cek kotak masuk Anda dan klik tautan tersebut untuk mengaktifkan akun Anda.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBackToLogin,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF004E))
                ) {
                    Text("Kembali ke Sign In", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Kirim Ulang
                TextButton(
                    onClick = viewModel::resendVerificationEmail,
                    enabled = !isResendDisabled // Gunakan isResendDisabled
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (cooldown > 0) {
                        // ✅ Tampilkan hitungan mundur
                        Text("Kirim Ulang Email dalam (${cooldown}s)")
                    } else {
                        // ✅ Tampilkan tombol normal
                        Text("Kirim Ulang Email")
                    }
                }
            }
        }
    }
}