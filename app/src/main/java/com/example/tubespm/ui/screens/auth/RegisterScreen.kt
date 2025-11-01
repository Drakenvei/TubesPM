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
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.authEvent.collectLatest { event ->
            when (event) {
                is AuthEvent.NavigateToMain -> {
                    navController.navigate("main") {
                        popUpTo("register") { inclusive = true }
                        // Jika register, mungkin ingin popUpTo("login") agar tidak bisa kembali
                        // popUpTo("login") { inclusive = true }
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
                TextButton(onClick = { navController.navigate("login") }) {
                    Text("Sign in", color = Color.White)
                }
                TextButton(onClick = { /* already here */ }) {
                    Text("Sign up", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Get started", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Let's create your account!", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))

            // ... (UI untuk semua OutlinedTextField)
            OutlinedTextField(value = name, onValueChange = { name = it }, /* ... */)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, /* ... */)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, /* ... */)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = confirm, onValueChange = { confirm = it }, /* ... */)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.register(name, email, password, confirm)
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF004E))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Sign up", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

// Tambahkan @Composable untuk OutlinedTextField yang berulang
// (Saya singkat di sini agar tidak terlalu panjang, tapi UI-nya sama seperti di LoginScreen)

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    TubesPMTheme {
        RegisterScreen(navController = rememberNavController())
    }
}