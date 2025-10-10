package com.example.tubespm


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.MainScreen
import com.example.tubespm.ui.theme.TubesPMTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import com.example.tubespm.ui.screens.HomeScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TubesPMTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("get_started") { GetStartedPage(onClick = { navController.navigate("login") }) }
        composable("login") { LoginScreen(navController = navController, auth = auth) }
        composable("register") {
            RegisterScreen(
                navController = navController,
                auth = auth,
                db = db
            )
        }
        composable("main") { MainScreen() } // placeholder after successful login
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    // 2 second splash -> navigate to get_started
    LaunchedEffect(key1 = true) {
        delay(1500)
        navController.navigate("get_started") {
            popUpTo("splash") { inclusive = true }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(80.dp))
        )
    }
}

@Composable
fun GetStartedPage(onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFFF6F6))) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(100.dp))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFF0057), Color(0xFFFF7E30))),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Hi, Champions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque vel.",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF004E))
                ) {
                    Text("Get Started", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController, auth: FirebaseAuth) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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
            Spacer(modifier = Modifier.height(60.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(25.dp))
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                TextButton(onClick = { /* already here */ }) {
                    Text(
                        "Sign in",
                        color = Color.Black
                    )
                }
                TextButton(onClick = { navController.navigate("register") }) {
                    Text(
                        "Sign up",
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Welcome back,",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Good to see you again!", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))

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
            Text(
                "Forgot password?",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Enter email and password", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Signed in", Toast.LENGTH_SHORT).show()
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Login failed: ${task.exception?.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF004E))
            ) {
                Text("Sign in", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController, auth: FirebaseAuth, db: FirebaseFirestore) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(60.dp))
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(25.dp))
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                TextButton(onClick = { navController.navigate("login") }) {
                    Text(
                        "Sign in",
                        color = Color.White
                    )
                }
                TextButton(onClick = { /* already here */ }) {
                    Text(
                        "Sign up",
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Get started", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Let's create your account!", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        passwordVisible = !passwordVisible
                    }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password != confirm) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                val uid = user?.uid
                                if (uid != null) {
                                    val userMap = mapOf(
                                        "name" to name.trim(),
                                        "email" to email.trim()
                                    )
                                    db.collection("users").document(uid).set(userMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Account created",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("main") {
                                                popUpTo("register") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "User data save failed: ${e.localizedMessage}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "User created but UID is missing",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Register failed: ${task.exception?.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF004E))
            ) {
                Text("Sign up", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    TubesPMTheme {
        // We pass a dummy NavController for the preview to work
        SplashScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun GetStartedPagePreview() {
    TubesPMTheme {
        // We pass an empty lambda for the onClick action in the preview
        GetStartedPage(onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TubesPMTheme {
        LoginScreen(
            navController = rememberNavController(),
            auth = FirebaseAuth.getInstance() // Preview needs a non-null instance
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    TubesPMTheme {
        RegisterScreen(
            navController = rememberNavController(),
            auth = FirebaseAuth.getInstance(),
            db = FirebaseFirestore.getInstance()
        )
    }
}


