package com.tscorp.pokus.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PreferencesManager.PREFERENCES_NAME
)

/**
 * Manages application preferences using DataStore.
 * Handles focus mode state and other app settings.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    /**
     * Flow that emits the current focus mode state.
     */
    val isFocusModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_FOCUS_MODE_ENABLED] ?: false
    }

    /**
     * Flow that emits the timestamp when focus mode was last enabled.
     */
    val focusModeStartTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[KEY_FOCUS_MODE_START_TIME] ?: 0L
    }

    /**
     * Flow that emits whether onboarding has been completed.
     */
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Flow that emits whether to show system apps in the app list.
     */
    val showSystemApps: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_SHOW_SYSTEM_APPS] ?: false
    }

    /**
     * Enable or disable focus mode.
     */
    suspend fun setFocusModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_FOCUS_MODE_ENABLED] = enabled
            if (enabled) {
                preferences[KEY_FOCUS_MODE_START_TIME] = System.currentTimeMillis()
            }
        }
    }

    /**
     * Mark onboarding as completed.
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    /**
     * Set whether to show system apps in the app list.
     */
    suspend fun setShowSystemApps(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_SYSTEM_APPS] = show
        }
    }

    /**
     * Reset focus mode start time (used when calculating focus duration).
     */
    suspend fun resetFocusModeStartTime() {
        dataStore.edit { preferences ->
            preferences[KEY_FOCUS_MODE_START_TIME] = 0L
        }
    }

    companion object {
        const val PREFERENCES_NAME = "pokus_preferences"

        private val KEY_FOCUS_MODE_ENABLED = booleanPreferencesKey("focus_mode_enabled")
        private val KEY_FOCUS_MODE_START_TIME = longPreferencesKey("focus_mode_start_time")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_SHOW_SYSTEM_APPS = booleanPreferencesKey("show_system_apps")
    }
}
