package com.example.easycooking.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_IS_METRIC = booleanPreferencesKey("is_metric_unit")
        val KEY_STRICT_ALLERGEN = booleanPreferencesKey("strict_allergen_filter")
        val KEY_SELECTED_ALLERGENS = stringSetPreferencesKey("selected_allergens")
        val KEY_SELECTED_DIETARY = stringSetPreferencesKey("selected_dietary")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_LOGGED_IN_EMAIL = stringPreferencesKey("logged_in_email")
        val KEY_LOGGED_IN_NAME = stringPreferencesKey("logged_in_name")
        val KEY_DB_SEEDED = booleanPreferencesKey("is_database_seeded")
        val KEY_SEED_VERSION = intPreferencesKey("seed_version")
        val KEY_ALARM_SOUND_ID = stringPreferencesKey("alarm_sound_id")
        val KEY_CUSTOM_ALARM_PATH = stringPreferencesKey("custom_alarm_path")
        val KEY_CUSTOM_ALARM_NAME = stringPreferencesKey("custom_alarm_name")
        val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
        val KEY_REMEMBERED_EMAIL = stringPreferencesKey("remembered_email")
    }

    val isMetricUnitFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_METRIC] ?: true
    }

    val strictAllergenFilterFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_STRICT_ALLERGEN] ?: false
    }

    val selectedAllergensFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_ALLERGENS] ?: emptySet()
    }

    val selectedDietaryFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_DIETARY] ?: emptySet()
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val loggedInEmailFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_LOGGED_IN_EMAIL] ?: ""
        CryptoBox.decrypt(raw)
    }

    val loggedInNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_LOGGED_IN_NAME] ?: ""
        CryptoBox.decrypt(raw)
    }

    val alarmSoundIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_ALARM_SOUND_ID] ?: "double_beep"
    }

    val customAlarmPathFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_ALARM_PATH] ?: ""
    }

    val customAlarmNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_ALARM_NAME] ?: ""
    }

    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_REMEMBER_ME] ?: false
    }

    val rememberedEmailFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_REMEMBERED_EMAIL] ?: ""
        CryptoBox.decrypt(raw)
    }

    suspend fun isDatabaseSeeded(): Boolean {
        return context.dataStore.data.first()[KEY_DB_SEEDED] ?: false
    }

    suspend fun setDatabaseSeeded(seeded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DB_SEEDED] = seeded
        }
    }

    suspend fun getSeedVersion(): Int {
        return context.dataStore.data.first()[KEY_SEED_VERSION] ?: 0
    }

    suspend fun setSeedVersion(version: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SEED_VERSION] = version
        }
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

    suspend fun setLoggedInUser(email: String, name: String, rememberMe: Boolean = false) {
        val encEmail = CryptoBox.encrypt(email)
        val encName = CryptoBox.encrypt(name)
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_LOGGED_IN_EMAIL] = encEmail
            preferences[KEY_LOGGED_IN_NAME] = encName
            preferences[KEY_REMEMBER_ME] = rememberMe
            preferences[KEY_REMEMBERED_EMAIL] = if (rememberMe) encEmail else ""
        }
    }

    suspend fun clearLoggedInUser(clearRememberedEmail: Boolean = false) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = false
            preferences[KEY_LOGGED_IN_EMAIL] = ""
            preferences[KEY_LOGGED_IN_NAME] = ""
            if (clearRememberedEmail) {
                preferences[KEY_REMEMBER_ME] = false
                preferences[KEY_REMEMBERED_EMAIL] = ""
            }
        }
    }

    suspend fun isRememberMeEnabled(): Boolean {
        return context.dataStore.data.first()[KEY_REMEMBER_ME] ?: false
    }

    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data.first()[KEY_IS_LOGGED_IN] ?: false
    }

    suspend fun setAlarmSoundPreset(soundId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALARM_SOUND_ID] = soundId
        }
    }

    suspend fun setCustomAlarmAudio(filePath: String, displayName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALARM_SOUND_ID] = "custom"
            preferences[KEY_CUSTOM_ALARM_PATH] = filePath
            preferences[KEY_CUSTOM_ALARM_NAME] = displayName
        }
    }
}
