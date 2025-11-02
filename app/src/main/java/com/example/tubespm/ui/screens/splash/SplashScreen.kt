package com.example.tubespm.ui.screens.splash

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.theme.TubesPMTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    var startAnimation by remember { mutableStateOf(false) }

    val offsetY by animateDpAsState(
        targetValue = if (startAnimation)(-100.dp) else 0.dp,
        animationSpec = tween(durationMillis = 800)
    )

    val alpha by animateDpAsState(
        targetValue = if (startAnimation) (-100).dp else 0.dp,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(true) {
        startAnimation = true
        delay(2000)
        navController.navigate("get_started") {
            popUpTo("splash") { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        navController.navigate("get_started") {
            popUpTo("splash") { inclusive = true}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F6)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = com.example.tubespm.R.drawable.logobelut),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .offset(y = offsetY)
                .clip(RoundedCornerShape(100.dp))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    TubesPMTheme {
        SplashScreen(navController = rememberNavController())
    }
}