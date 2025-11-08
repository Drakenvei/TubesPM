package com.example.tubespm.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
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
fun LoginContent(
    email: String,
    pass: String,
    error: String?,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onPassChanged: (String) -> Unit,
    onLoginClicked: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome back,", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Good to see you again!", color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))

        // Email TextField dengan floating label
        val emailElevation by animateDpAsState(
            targetValue = if (email.isNotEmpty() || emailFocused) 8.dp else 2.dp,
            animationSpec = tween(300),
            label = "emailElevation"
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChanged,
            label = {
                Text(
                    "Email",
                    color = if (email.isNotEmpty() || emailFocused) Color.White else Color.Gray.copy(alpha = 0.6f),
                    fontWeight = if (email.isNotEmpty() || emailFocused) FontWeight.Medium else FontWeight.Normal
                )
            },
            isError = error != null, // <-- DITAMBAHKAN: Tampilkan error
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = emailElevation,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .onFocusChanged { emailFocused = it.isFocused },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                focusedBorderColor = Color(0xFFFF004E).copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,

                // -- WARNA SAAT ERROR (KUNCI UTAMANYA DI SINI) --
                errorContainerColor = Color.White.copy(alpha = 0.95f), // Background tetap putih
                errorBorderColor = Color.White, // Border PUTIH saat error
                errorLabelColor = Color.White, // Label "Email" tetap putih
                errorTextColor = Color.Black // Teks yang diketik tetap hitam
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField dengan floating label
        val passwordElevation by animateDpAsState(
            targetValue = if (pass.isNotEmpty() || passwordFocused) 8.dp else 2.dp,
            animationSpec = tween(300),
            label = "passwordElevation"
        )

        OutlinedTextField(
            value = pass,
            onValueChange = onPassChanged,
            label = {
                Text(
                    "Password",
                    color = if (pass.isNotEmpty() || passwordFocused) Color.White else Color.Gray.copy(alpha = 0.6f),
                    fontWeight = if (pass.isNotEmpty() || passwordFocused) FontWeight.Medium else FontWeight.Normal
                )
            },
            isError = error != null, // <-- DITAMBAHKAN: Tampilkan error,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = passwordElevation,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .onFocusChanged { passwordFocused = it.isFocused },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility",
                        tint = Color.Gray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                focusedBorderColor = Color(0xFFFF004E).copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,

                // -- WARNA SAAT ERROR (KUNCI UTAMANYA DI SINI) --
                errorContainerColor = Color.White.copy(alpha = 0.95f), // Background tetap putih
                errorBorderColor = Color.White, // Border PUTIH saat error
                errorLabelColor = Color.White, // Label "Email" tetap putih
                errorTextColor = Color.Black // Teks yang diketik tetap hitam
            )
        )

        if (error != null){
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error, // Gunakan warna error dari tema
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.Start)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text("Forgot password?", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLoginClicked,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF004E))
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign in", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}