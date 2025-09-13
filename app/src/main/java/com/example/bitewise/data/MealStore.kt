package com.example.bitewise.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// We’ll store meals as newline-delimited lines: id|name|calories|category|timestamp
// Simple & dependency-free for now.

private val Context.dataStore by preferencesDataStore("meal_store")
private val MEALS_KEY = stringPreferencesKey("meals_lines")

data class MealEntry(
    val id: Long,          // unique id (epoch millis)
    val name: String,
    val calories: Int,
    val category: String,  // "Low Effort" | "Healthy" | "Indulgent" | ...
    val timestamp: Long    // epoch millis
)

private fun encode(meal: MealEntry): String =
    listOf(meal.id, meal.name, meal.calories, meal.category, meal.timestamp)
        .joinToString("|")

private fun decode(line: String): MealEntry? = try {
    val p = line.split("|")
    MealEntry(
        id = p[0].toLong(),
        name = p[1],
        calories = p[2].toInt(),
        category = p[3],
        timestamp = p[4].toLong()
    )
} catch (_: Exception) { null }

private fun parseAll(s: String): List<MealEntry> =
    if (s.isBlank()) emptyList() else s.split("\n").mapNotNull(::decode)

private fun formatAll(list: List<MealEntry>): String =
    list.joinToString("\n") { encode(it) }

class MealStore(private val context: Context) {

    val mealsFlow: Flow<List<MealEntry>> =
        context.dataStore.data.map { prefs ->
            parseAll(prefs[MEALS_KEY] ?: "")
        }

    suspend fun addMeal(name: String, calories: Int, category: String) {
        val now = System.currentTimeMillis()
        val entry = MealEntry(
            id = now,
            name = name.trim(),
            calories = calories,
            category = category.trim(),
            timestamp = now
        )
        context.dataStore.edit { prefs ->
            val current = parseAll(prefs[MEALS_KEY] ?: "")
            prefs[MEALS_KEY] = formatAll(current + entry)
        }
    }

    suspend fun removeMeal(id: Long) {
        context.dataStore.edit { prefs ->
            val current = parseAll(prefs[MEALS_KEY] ?: "")
            prefs[MEALS_KEY] = formatAll(current.filterNot { it.id == id })
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it[MEALS_KEY] = "" }
    }
}
