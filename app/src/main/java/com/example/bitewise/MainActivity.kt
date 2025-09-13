package com.example.bitewise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bitewise.Ui.*
import com.example.bitewise.core.bmi
import com.example.bitewise.core.bmiCategory
import com.example.bitewise.data.RecipeType
import com.example.bitewise.data.UserPrefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private enum class Route { Splash, Home, Main }
private enum class Tab { Profile, Ingredients, Meals, Scan }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (w, h) = runBlocking { UserPrefs(this@MainActivity).flow.first() }
        val category = bmiCategory(bmi(w, h))

        setContent {
            MaterialTheme {
                var route by remember { mutableStateOf(Route.Splash) }
                when (route) {
                    Route.Splash -> SplashScreen { route = Route.Home }
                    Route.Home   -> HomeScreen   { route = Route.Main }
                    Route.Main   -> MainScreen(category)
                }
            }
        }
    }
}

//composable function
@Composable
private fun MainScreen(category: String) {
    var tab by remember { mutableStateOf(Tab.Profile) }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = { tab = Tab.Profile })    { Text("Profile") }
                    TextButton(onClick = { tab = Tab.Ingredients }) { Text("Recipes") }
                    TextButton(onClick = { tab = Tab.Meals })       { Text("Meals") }
                    TextButton(onClick = { tab = Tab.Scan })        { Text("Scan") }   // 👈 new
                }
            }
        }
    ) { inner ->
        Box(Modifier.padding(inner).padding(12.dp)) {
            when (tab) {
                Tab.Profile -> ProfileScreen()
                Tab.Ingredients -> IngredientsScreen(
                    defaultType = when (category) {
                        "Underweight" -> RecipeType.Indulgent
                        "Healthy"     -> null
                        "Overweight", "Obese" -> RecipeType.Healthy
                        else -> null
                    }
                )
                Tab.Meals -> MealsScreen(bmiCategory = category)
                Tab.Scan        -> ScanScreen()
            }
        }
    }
}
