package com.example.bitewise.data


import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

object UserKeys {
    val WEIGHT_KG = doublePreferencesKey("weightKg")
    val HEIGHT_CM = doublePreferencesKey("heightCm")
}

class UserPrefs(private val context: Context) {
    val flow = context.dataStore.data.map { p ->
        (p[UserKeys.WEIGHT_KG] ?: 70.0) to (p[UserKeys.HEIGHT_CM] ?: 170.0)
    }

    suspend fun save(weightKg: Double, heightCm: Double) {
        context.dataStore.edit {
            it[UserKeys.WEIGHT_KG] = weightKg
            it[UserKeys.HEIGHT_CM] = heightCm
        }
    }
}