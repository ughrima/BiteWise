package com.example.bitewise.Ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bitewise.core.bmi
import com.example.bitewise.core.bmiCategory
import com.example.bitewise.data.UserPrefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen() {
    val ctx = LocalContext.current
    val prefs = remember { UserPrefs(ctx) }
    val scope = rememberCoroutineScope()

    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    // Load saved values once on first composition
    LaunchedEffect(Unit) {
        val (w, h) = prefs.flow.first()
        weight = if (weight.isBlank()) w.toString() else weight
        height = if (height.isBlank()) h.toString() else height
    }

    val w = weight.toDoubleOrNull() ?: 0.0
    val h = height.toDoubleOrNull() ?: 0.0
    val bmiVal = bmi(w, h)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Your Profile", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Text("BMI: $bmiVal (${bmiCategory(bmiVal)})", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { scope.launch { prefs.save(w, h) } }) { Text("Save") }
            OutlinedButton(onClick = { weight = ""; height = "" }) { Text("Reset") }
        }
    }
}
