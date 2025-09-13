package com.example.bitewise.Ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bitewise.data.MealEntry
import com.example.bitewise.data.MealStore
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(bmiCategory: String) {
    val ctx = LocalContext.current
    val store = remember { MealStore(ctx) }
    val scope = rememberCoroutineScope()

    val allMeals by store.mealsFlow.collectAsState(initial = emptyList())
    val todayMeals = remember(allMeals) { allMeals.filter { isToday(it.timestamp) } }
    val total = remember(todayMeals) { todayMeals.sumOf { it.calories } }

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Meals Today", style = MaterialTheme.typography.headlineMedium)
        AssistChip(
            onClick = {},
            label = { Text("BMI: $bmiCategory") }
        )
        Text("Total calories: $total", style = MaterialTheme.typography.titleMedium)

        Button(onClick = { showDialog = true }) { Text("Add meal") }

        Divider()

        if (todayMeals.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No meals logged yet. Tap \"Add meal\" to start.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(todayMeals, key = { it.id }) { meal ->
                    MealRow(
                        meal = meal,
                        onDelete = { scope.launch { store.removeMeal(meal.id) } }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddMealDialog(
            onDismiss = { showDialog = false },
            onSave = { name, calories, category ->
                scope.launch {
                    store.addMeal(name, calories, category)
                    showDialog = false
                }
            }
        )
    }
}

@Composable
private fun MealRow(meal: MealEntry, onDelete: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(meal.name, style = MaterialTheme.typography.titleMedium)
                Text("${meal.category} • ${meal.calories} kcal", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "Delete",
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable { onDelete() },
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AddMealDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, calories: Int, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    val categories = listOf("Low Effort", "Healthy", "Indulgent")
    var expanded by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(categories.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add meal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                // Simple dropdown
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    category = c
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cal = caloriesText.toIntOrNull() ?: 0
                if (name.isNotBlank() && cal > 0) {
                    onSave(name.trim(), cal, category)
                } else onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun isToday(ts: Long): Boolean {
    val a = Calendar.getInstance()
    val b = Calendar.getInstance().apply { timeInMillis = ts }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
