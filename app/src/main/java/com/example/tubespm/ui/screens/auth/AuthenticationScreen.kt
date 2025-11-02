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
//            Box(
//                modifier = Modifier
//                    .size(50.dp)
//                    .background(Color.LightGray, shape = RoundedCornerShape(25.dp))
//            )
            Image(
                painter = painterResource(id = com.example.tubespm.R.drawable.logobelut3),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)

            )
            Spacer(modifier = Modifier.height(20.dp))

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