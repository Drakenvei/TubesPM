package com.example.tubespm.ui.screens.auth

import androidx.compose.ui.platform.LocalConfiguration
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.tubespm.R

private enum class AuthView {
    SIGN_IN,
    SIGN_UP,
    VERIFICATION_SENT, // Setelah Register
    VERIFY_LOGIN_PROMPT // Setelah Gagal Login (belum verifikasi)
}

@Composable
fun AuthenticationScreen(
    onAuthSuccess: (String)-> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var currentView by remember { mutableStateOf(AuthView.SIGN_IN) }
    val uiState by viewModel.uiState.collectAsState()

    // Dengarkan event satu kali (navigasi & toast)
    LaunchedEffect(key1 = true) {
        viewModel.authEvent.collectLatest { event ->
            when (event) {
                is AuthEvent.NavigateWithRole ->  {
                    onAuthSuccess(event.role)
                }
                is AuthEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is AuthEvent.RegistrationSuccess -> {
                    // Pindah ke layar konfirmasi verifikasi setelah daftar
                    currentView = AuthView.VERIFICATION_SENT
                }
                // Tangani event verifikasi dari login
                is AuthEvent.VerifyEmailPrompt -> {
                    // Pindah ke prompt verifikasi.
                    currentView = AuthView.VERIFY_LOGIN_PROMPT
                }
            }
        }
    }

    val gradient = Brush.verticalGradient(listOf(Color(0xFFFF004E), Color(0xFFFF7E30)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 32.dp)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Image(
                painter = painterResource(id = com.example.tubespm.R.drawable.logobelut3),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Tampilkan Toggle Button HANYA di mode SIGN_IN dan SIGN_UP
            if (currentView != AuthView.VERIFICATION_SENT && currentView != AuthView.VERIFY_LOGIN_PROMPT) {
                // Tombol Toggle dengan animasi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(4.dp)
                ) {
                    // Animated background indicator
                    val indicatorOffset by animateDpAsState(
                        targetValue = if (currentView == AuthView.SIGN_IN) 0.dp else (LocalConfiguration.current.screenWidthDp.dp - 64.dp) / 2,
                        animationSpec = tween(300),
                        label = "indicator"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width((LocalConfiguration.current.screenWidthDp.dp - 64.dp) / 2 - 4.dp)
                            .offset(x = indicatorOffset)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(24.dp)
                            )
                    )

                    // Toggle buttons
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    onClick = { currentView = AuthView.SIGN_IN },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val textColor by animateColorAsState(
                                targetValue = if (currentView == AuthView.SIGN_IN) Color.Black else Color.White,
                                animationSpec = tween(300),
                                label = "signInColor"
                            )
                            Text(
                                "Sign In",
                                color = textColor,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (currentView == AuthView.SIGN_IN)
                                        androidx.compose.ui.text.font.FontWeight.Bold
                                    else
                                        androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    onClick = { currentView = AuthView.SIGN_UP },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val textColor by animateColorAsState(
                                targetValue = if (currentView == AuthView.SIGN_UP) Color.Black else Color.White,
                                animationSpec = tween(300),
                                label = "signUpColor"
                            )
                            Text(
                                "Sign Up",
                                color = textColor,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (currentView == AuthView.SIGN_UP)
                                        androidx.compose.ui.text.font.FontWeight.Bold
                                    else
                                        androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                // Beri jarak jika Toggle Button tidak ada
                Spacer(modifier = Modifier.height(96.dp))
            }


            // Konten yang dapat berganti
            AnimatedContent(
                targetState = currentView,
                label = "AuthContentSwitch",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier.fillMaxWidth()
            ) { view ->
                when (view) {
                    AuthView.SIGN_IN -> {
                        LoginContent(
                            email = uiState.loginEmail,
                            pass = uiState.loginPass,
                            error = uiState.loginError,
                            isLoading = uiState.isLoading,
                            onEmailChanged = viewModel::onLoginEmailChanged,
                            onPassChanged = viewModel::onLoginPassChanged,
                            onLoginClicked = viewModel::login,
                            onForgotPasswordClicked = viewModel::sendPasswordReset
                        )
                    }
                    AuthView.SIGN_UP -> {
                        RegisterContent(
                            name = uiState.regName,
                            email = uiState.regEmail,
                            pass = uiState.regPass,
                            confirm = uiState.regConfirm,
                            error = uiState.regError,
                            isLoading = uiState.isLoading,
                            onNameChanged = viewModel::onRegNameChanged,
                            onEmailChanged = viewModel::onRegEmailChanged,
                            onPassChanged = viewModel::onRegPassChanged,
                            onConfirmChanged = viewModel::onRegConfirmChanged,
                            onRegisterClicked = viewModel::register
                        )
                    }
                    // Screen Verifikasi setelah Register
                    AuthView.VERIFICATION_SENT -> {
                        VerificationSentScreen(
                            onBackToLogin = {
                                currentView = AuthView.SIGN_IN
                                // Opsional: Reset input register agar bersih
                                viewModel.onRegEmailChanged("")
                                viewModel.onRegPassChanged("")
                                viewModel.onRegConfirmChanged("")
                            },
                            displayEmail = uiState.regEmail
                        )
                    }
                    // Screen Verifikasi saat Gagal Login
                    AuthView.VERIFY_LOGIN_PROMPT -> {
                        VerificationSentScreen( // Re-use the same UI Composable
                            onBackToLogin = { currentView = AuthView.SIGN_IN },
                            displayEmail = uiState.loginEmail,
                            isLoginAttempt = true // Set flag untuk ubah teks
                        )
                    }
                }
            }
        }
    }
}