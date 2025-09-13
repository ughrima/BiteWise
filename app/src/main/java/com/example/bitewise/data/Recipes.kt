package com.example.bitewise.data

data class Macros(val calories: Int, val protein: Int, val carbs: Int, val fat: Int)
enum class RecipeType { LowEffort, Healthy, Indulgent }

data class Recipe(
    val id: String,
    val name: String,
    val ingredients: Set<String>, // lowercase
    val type: RecipeType,
    val timeMins: Int,
    val macros: Macros
)

private fun s(vararg items: String) = items.map { it.trim().lowercase() }.toSet()

// 👇 Seed data — tweak freely
val LocalRecipes: List<Recipe> = listOf(
    Recipe(
        id = "oats_pb",
        name = "Peanut Butter Oats",
        ingredients = s("oats", "milk", "banana", "peanut butter"),
        type = RecipeType.Healthy,
        timeMins = 10,
        macros = Macros(calories = 380, protein = 14, carbs = 50, fat = 12)
    ),
    Recipe(
        id = "veg_wrap",
        name = "Quick Veg Wrap",
        ingredients = s("tortilla", "tomato", "lettuce", "onion", "cheese"),
        type = RecipeType.LowEffort,
        timeMins = 8,
        macros = Macros(320, 12, 42, 10)
    ),
    Recipe(
        id = "paneer_stir",
        name = "Paneer Stir-Fry",
        ingredients = s("paneer", "capsicum", "onion", "oil", "soy sauce"),
        type = RecipeType.Healthy,
        timeMins = 18,
        macros = Macros(420, 28, 18, 24)
    ),
    Recipe(
        id = "choco_mug",
        name = "Choco Mug Cake",
        ingredients = s("flour", "cocoa", "milk", "sugar"),
        type = RecipeType.Indulgent,
        timeMins = 5,
        macros = Macros(450, 8, 66, 16)
    ),
)

fun score(byIngredients: Set<String>, recipe: Recipe): Int =
    (byIngredients intersect recipe.ingredients).size

fun normalizeInput(input: String): Set<String> =
    input.split(",", " ", ";", "\n")
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .toSet()
