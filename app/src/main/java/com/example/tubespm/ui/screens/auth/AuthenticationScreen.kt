package com.example.tubespm.ui.screens.auth

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

private enum class AuthView {
    SIGN_IN,
    SIGN_UP
}

@Composable
fun AuthenticationScreen(
    onAuthSuccess: ()-> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var currentView by remember { mutableStateOf(AuthView.SIGN_IN) }
    val uiState by viewModel.uiState.collectAsState()

    // Dengarkan event satu kali (navigasi & toast)
    LaunchedEffect(key1 = true) {
        viewModel.authEvent.collectLatest { event ->
            when (event) {
                is AuthEvent.NavigateToMain ->  {
                    onAuthSuccess()
                }
                is AuthEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val gradient = Brush.verticalGradient(listOf(Color(0xFFFF004E), Color(0xFFFF7E30)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(25.dp))
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Toggle
            Row {
                TextButton(
                    onClick = {currentView = AuthView.SIGN_IN}
                ) {
                    Text(
                        "Sign in",
                        color = if (currentView == AuthView.SIGN_IN) Color.Black else Color.White
                    )
                }
                TextButton(onClick = { currentView = AuthView.SIGN_UP }) {
                    Text(
                        "Sign up",
                        color = if (currentView == AuthView.SIGN_UP) Color.Black else Color.White
                    )
                }
            }

            // Konten yang dapat berganti (Login atau Register)
            // AnimatedContent memberikan efek fade-in/out yang halus
            AnimatedContent(
                targetState = currentView,
                label = "AuthContentSwitch",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                }
            ) { view ->
                if (view == AuthView.SIGN_IN) {
                    LoginContent(
                        uiState = uiState,
                        onLoginClicked = { email, pass ->
                            viewModel.login(email, pass)
                        }
                    )
                } else {
                    RegisterContent(
                        uiState = uiState,
                        onRegisterClicked = { name, email, pass, confirm ->
                            viewModel.register(name, email, pass, confirm)
                        }
                    )
                }
            }
        }
    }
}