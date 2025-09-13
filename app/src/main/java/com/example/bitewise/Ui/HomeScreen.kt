package com.example.bitewise.Ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)         // 2 seconds
        onTimeout()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🍽️", fontSize = 48.sp) // simple “catchy logo”
            Text("BiteWise", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Your digital nutrition companion", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HomeScreen(onGetStarted: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("BiteWise", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Your digital nutrition companion")
            Button(onClick = onGetStarted) { Text("Let’s get started") }
        }
    }
}
