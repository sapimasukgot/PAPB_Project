package com.example.easycooking.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_IS_METRIC = booleanPreferencesKey("is_metric_unit")
        val KEY_STRICT_ALLERGEN = booleanPreferencesKey("strict_allergen_filter")
        val KEY_SELECTED_ALLERGENS = stringSetPreferencesKey("selected_allergens")
        val KEY_SELECTED_DIETARY = stringSetPreferencesKey("selected_dietary")
    }

    val isMetricUnitFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_METRIC] ?: true
    }

    val strictAllergenFilterFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_STRICT_ALLERGEN] ?: true
    }

    val selectedAllergensFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_ALLERGENS] ?: setOf("Peanuts", "Tree Nuts")
    }

    val selectedDietaryFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_DIETARY] ?: setOf("Vegetarian", "Gluten-Free")
    }

    suspend fun setMetricUnit(isMetric: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_METRIC] = isMetric
        }
    }

    suspend fun setStrictAllergenFilter(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_STRICT_ALLERGEN] = enabled
        }
    }

    suspend fun setSelectedAllergens(allergens: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_ALLERGENS] = allergens
        }
    }

    suspend fun setSelectedDietary(dietary: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_DIETARY] = dietary
        }
    }
}
