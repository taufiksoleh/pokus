package com.tscorp.pokus.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    // Pomodoro Timer Preferences

    /**
     * Flow that emits whether Pomodoro timer is enabled.
     */
    val isPomodoroEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_POMODORO_ENABLED] ?: false
    }

    /**
     * Flow that emits the work duration in minutes.
     */
    val pomodoroWorkDuration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_POMODORO_WORK_DURATION] ?: 25
    }

    /**
     * Flow that emits the short break duration in minutes.
     */
    val pomodoroShortBreak: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_POMODORO_SHORT_BREAK] ?: 5
    }

    /**
     * Flow that emits the long break duration in minutes.
     */
    val pomodoroLongBreak: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_POMODORO_LONG_BREAK] ?: 15
    }

    /**
     * Flow that emits the number of pomodoros until a long break.
     */
    val pomodorosUntilLongBreak: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_POMODOROS_UNTIL_LONG_BREAK] ?: 4
    }

    /**
     * Flow that emits whether to auto-start breaks.
     */
    val pomodoroAutoStartBreaks: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_POMODORO_AUTO_START_BREAKS] ?: false
    }

    /**
     * Flow that emits whether to auto-start pomodoros.
     */
    val pomodoroAutoStartPomodoros: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_POMODORO_AUTO_START_POMODOROS] ?: false
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

    // Pomodoro Timer Setters

    /**
     * Enable or disable Pomodoro timer.
     */
    suspend fun setPomodoroEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_POMODORO_ENABLED] = enabled
        }
    }

    /**
     * Set work duration in minutes.
     */
    suspend fun setPomodoroWorkDuration(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_POMODORO_WORK_DURATION] = minutes
        }
    }

    /**
     * Set short break duration in minutes.
     */
    suspend fun setPomodoroShortBreak(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_POMODORO_SHORT_BREAK] = minutes
        }
    }

    /**
     * Set long break duration in minutes.
     */
    suspend fun setPomodoroLongBreak(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_POMODORO_LONG_BREAK] = minutes
        }
    }

    /**
     * Set number of pomodoros until a long break.
     */
    suspend fun setPomodorosUntilLongBreak(count: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_POMODOROS_UNTIL_LONG_BREAK] = count
        }
    }

    /**
     * Set whether to auto-start breaks.
     */
    suspend fun setPomodoroAutoStartBreaks(autoStart: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_POMODORO_AUTO_START_BREAKS] = autoStart
        }
    }

    /**
     * Set whether to auto-start pomodoros.
     */
    suspend fun setPomodoroAutoStartPomodoros(autoStart: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_POMODORO_AUTO_START_POMODOROS] = autoStart
        }
    }

    companion object {
        const val PREFERENCES_NAME = "pokus_preferences"

        private val KEY_FOCUS_MODE_ENABLED = booleanPreferencesKey("focus_mode_enabled")
        private val KEY_FOCUS_MODE_START_TIME = longPreferencesKey("focus_mode_start_time")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_SHOW_SYSTEM_APPS = booleanPreferencesKey("show_system_apps")

        // Pomodoro Timer Keys
        private val KEY_POMODORO_ENABLED = booleanPreferencesKey("pomodoro_enabled")
        private val KEY_POMODORO_WORK_DURATION = intPreferencesKey("pomodoro_work_duration")
        private val KEY_POMODORO_SHORT_BREAK = intPreferencesKey("pomodoro_short_break")
        private val KEY_POMODORO_LONG_BREAK = intPreferencesKey("pomodoro_long_break")
        private val KEY_POMODOROS_UNTIL_LONG_BREAK = intPreferencesKey("pomodoros_until_long_break")
        private val KEY_POMODORO_AUTO_START_BREAKS = booleanPreferencesKey("pomodoro_auto_start_breaks")
        private val KEY_POMODORO_AUTO_START_POMODOROS = booleanPreferencesKey("pomodoro_auto_start_pomodoros")
    }
}
