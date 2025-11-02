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
fun RegisterContent(
    uiState: AuthUiState,
    onRegisterClicked: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var nameFocused by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var confirmFocused by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Get started", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Let's create your account!", color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))

        // Name TextField dengan floating label
        val nameElevation by animateDpAsState(
            targetValue = if (name.isNotEmpty() || nameFocused) 8.dp else 2.dp,
            animationSpec = tween(300),
            label = "nameElevation"
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text(
                    "Your Name",
                    color = if (name.isNotEmpty() || nameFocused) Color.White else Color.Gray.copy(alpha = 0.6f),
                    fontWeight = if (name.isNotEmpty() || nameFocused) FontWeight.Medium else FontWeight.Normal
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = nameElevation,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .onFocusChanged { nameFocused = it.isFocused },
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
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email TextField dengan floating label
        val emailElevation by animateDpAsState(
            targetValue = if (email.isNotEmpty() || emailFocused) 8.dp else 2.dp,
            animationSpec = tween(300),
            label = "emailElevation"
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text(
                    "Email",
                    color = if (email.isNotEmpty() || emailFocused) Color.White else Color.Gray.copy(alpha = 0.6f),
                    fontWeight = if (email.isNotEmpty() || emailFocused) FontWeight.Medium else FontWeight.Normal
                )
            },
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
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password TextField dengan floating label
        val passwordElevation by animateDpAsState(
            targetValue = if (password.isNotEmpty() || passwordFocused) 8.dp else 2.dp,
            animationSpec = tween(300),
            label = "passwordElevation"
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(
                    "Password",
                    color = if (password.isNotEmpty() || passwordFocused) Color.White else Color.Gray.copy(alpha = 0.6f),
                    fontWeight = if (password.isNotEmpty() || passwordFocused) FontWeight.Medium else FontWeight.Normal
                )
            },
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
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password TextField dengan floating label
        val confirmElevation by animateDpAsState(
            targetValue = if (confirm.isNotEmpty() || confirmFocused) 8.dp else 2.dp,
            animationSpec = tween(300),
            label = "confirmElevation"
        )

        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = {
                Text(
                    "Confirm Password",
                    color = if (confirm.isNotEmpty() || confirmFocused) Color.White else Color.Gray.copy(alpha = 0.6f),
                    fontWeight = if (confirm.isNotEmpty() || confirmFocused) FontWeight.Medium else FontWeight.Normal
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = confirmElevation,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .onFocusChanged { confirmFocused = it.isFocused },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                unfocusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onRegisterClicked(name, email, password, confirm)
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