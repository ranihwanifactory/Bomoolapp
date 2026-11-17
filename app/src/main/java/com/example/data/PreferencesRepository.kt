package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "treasure_radar_prefs")

data class LocalSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val tutorialSeen: Boolean = false
)

class PreferencesRepository(private val context: Context) {
    private val keySound = booleanPreferencesKey("sound_enabled")
    private val keyVibration = booleanPreferencesKey("vibration_enabled")
    private val keyTutorial = booleanPreferencesKey("tutorial_seen")

    val settingsFlow: Flow<LocalSettings> = context.dataStore.data.map { prefs ->
        LocalSettings(
            soundEnabled = prefs[keySound] ?: true,
            vibrationEnabled = prefs[keyVibration] ?: true,
            tutorialSeen = prefs[keyTutorial] ?: false
        )
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keySound] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyVibration] = enabled }
    }

    suspend fun setTutorialSeen(seen: Boolean) {
        context.dataStore.edit { it[keyTutorial] = seen }
    }
}
