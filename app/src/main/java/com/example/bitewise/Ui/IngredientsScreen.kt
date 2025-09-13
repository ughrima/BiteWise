package com.example.bitewise.Ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bitewise.data.*

@Composable
fun IngredientsScreen(
    defaultType: RecipeType? = null,
) {
    var raw by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(defaultType) }
    var maxTime by remember { mutableStateOf(30) } // minutes

    val ingredients = remember(raw) { normalizeInput(raw) }
    val results = remember(ingredients, selectedType, maxTime) {
        LocalRecipes
            .asSequence()
            .filter { selectedType == null || it.type == selectedType }
            .filter { it.timeMins <= maxTime }
            .map { it to score(ingredients, it) }
            .filter { (_, sc) -> sc > 0 || ingredients.isEmpty() } // show all if no input
            .sortedWith(
                compareByDescending<Pair<Recipe, Int>> { it.second }
                    .thenBy { it.first.timeMins }
            )
            .map { it.first }
            .toList()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Find recipes", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = raw,
            onValueChange = { raw = it },
            label = { Text("Ingredients (comma-separated)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = false,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                label = { Text("All") },
                selected = selectedType == null,
                onClick = { selectedType = null }
            )
            FilterChip(
                label = { Text("Low Effort") },
                selected = selectedType == RecipeType.LowEffort,
                onClick = { selectedType = RecipeType.LowEffort }
            )
            FilterChip(
                label = { Text("Healthy") },
                selected = selectedType == RecipeType.Healthy,
                onClick = { selectedType = RecipeType.Healthy }
            )
            FilterChip(
                label = { Text("Indulgent") },
                selected = selectedType == RecipeType.Indulgent,
                onClick = { selectedType = RecipeType.Indulgent }
            )
        }

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Max time: $maxTime min")
            Slider(value = maxTime.toFloat(), onValueChange = { maxTime = it.toInt() }, valueRange = 5f..60f, steps = 10)
        }

        Divider()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(results, key = { it.id }) { recipe ->
                RecipeCard(recipe)
            }
        }
    }
}

@Composable
private fun FilterChip(label: @Composable () -> Unit, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = label,
        colors = if (selected) AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) else AssistChipDefaults.assistChipColors()
    )
}

@Composable
private fun RecipeCard(r: Recipe) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(r.name, style = MaterialTheme.typography.titleMedium)
            Text("${r.type} • ${r.timeMins} min")
            Text("Ingredients: " + r.ingredients.joinToString(", "))
            Text("Calories: ${r.macros.calories}  |  P:${r.macros.protein}g  C:${r.macros.carbs}g  F:${r.macros.fat}g")
            // (Optional later) Buttons: Save favorite / Log to Meals
        }
    }
}
