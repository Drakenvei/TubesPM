package com.example.tubespm.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.theme.TubesPMTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Dengarkan event satu kali (navigasi & toast)
    LaunchedEffect(key1 = true) {
        viewModel.authEvent.collectLatest { event ->
            when (event) {
                is AuthEvent.NavigateToMain -> {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                is AuthEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val gradient = Brush.verticalGradient(listOf(Color(0xFFFF004E), Color(0xFFFF7E30)))

    Box(modifier = Modifier
        .fillMaxSize()
        .background(gradient)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            // ... (UI untuk logo, tombol sign in/up, dll)
            Spacer(modifier = Modifier.height(60.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(25.dp))
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                TextButton(onClick = { /* already here */ }) {
                    Text("Sign in", color = Color.Black)
                }
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("Sign up", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Welcome back,", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Good to see you again!", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))

            // ... (UI untuk OutlinedTextField email & password)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Forgot password?", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                enabled = !uiState.isLoading, // Nonaktifkan tombol saat loading
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF004E))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Sign in", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TubesPMTheme {
        LoginScreen(navController = rememberNavController())
    }
}