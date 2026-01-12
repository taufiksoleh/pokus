package com.tscorp.pokus.ui.screens.pomodoro

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tscorp.pokus.data.preferences.PreferencesManager
import com.tscorp.pokus.domain.model.PomodoroPhase
import com.tscorp.pokus.domain.model.PomodoroSettings
import com.tscorp.pokus.domain.model.PomodoroState
import com.tscorp.pokus.service.PomodoroService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing Pomodoro timer state.
 * Communicates with PomodoroService and manages UI state.
 */
@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val application: Application,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    private val _pomodoroState = MutableStateFlow(PomodoroState.Idle)
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()

    /**
     * Pomodoro settings from preferences.
     */
    val pomodoroSettings: StateFlow<PomodoroSettings> = combine(
        preferencesManager.pomodoroWorkDuration,
        preferencesManager.pomodoroShortBreak,
        preferencesManager.pomodoroLongBreak,
        preferencesManager.pomodorosUntilLongBreak,
        preferencesManager.pomodoroAutoStartBreaks
    ) { workDuration, shortBreak, longBreak, pomodorosUntilLongBreak, autoStartBreaks ->
        PomodoroSettings(
            workDurationMinutes = workDuration,
            shortBreakMinutes = shortBreak,
            longBreakMinutes = longBreak,
            pomodorosUntilLongBreak = pomodorosUntilLongBreak,
            autoStartBreaks = autoStartBreaks,
            autoStartPomodoros = false // Will be updated below
        )
    }.combine(preferencesManager.pomodoroAutoStartPomodoros) { settings, autoStartPomodoros ->
        settings.copy(autoStartPomodoros = autoStartPomodoros)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PomodoroSettings.Default
    )

    // BroadcastReceiver to listen for timer updates from the service
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PomodoroService.ACTION_TIMER_UPDATE) {
                val isRunning = intent.getBooleanExtra(PomodoroService.EXTRA_IS_RUNNING, false)
                val isPaused = intent.getBooleanExtra(PomodoroService.EXTRA_IS_PAUSED, false)
                val phaseName = intent.getStringExtra(PomodoroService.EXTRA_PHASE)
                val remainingTime = intent.getLongExtra(PomodoroService.EXTRA_REMAINING_TIME, 0L)
                val totalTime = intent.getLongExtra(PomodoroService.EXTRA_TOTAL_TIME, 0L)
                val completedPomodoros = intent.getIntExtra(PomodoroService.EXTRA_COMPLETED_POMODOROS, 0)

                val phase = phaseName?.let { PomodoroPhase.valueOf(it) } ?: PomodoroPhase.IDLE

                _pomodoroState.value = PomodoroState(
                    isRunning = isRunning,
                    isPaused = isPaused,
                    phase = phase,
                    remainingTimeMillis = remainingTime,
                    totalTimeMillis = totalTime,
                    completedPomodoros = completedPomodoros
                )
            }
        }
    }

    init {
        // Register broadcast receiver to listen for timer updates
        val filter = IntentFilter(PomodoroService.ACTION_TIMER_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            application.registerReceiver(timerReceiver, filter)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            application.unregisterReceiver(timerReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    /**
     * Starts the Pomodoro timer with the given phase.
     */
    fun startTimer(phase: PomodoroPhase = PomodoroPhase.WORK) {
        PomodoroService.start(application, phase)
    }

    /**
     * Pauses the Pomodoro timer.
     */
    fun pauseTimer() {
        PomodoroService.pause(application)
    }

    /**
     * Resumes the Pomodoro timer.
     */
    fun resumeTimer() {
        PomodoroService.resume(application)
    }

    /**
     * Stops the Pomodoro timer.
     */
    fun stopTimer() {
        PomodoroService.stop(application)
        _pomodoroState.value = PomodoroState.Idle
    }

    /**
     * Skips the current phase.
     */
    fun skipPhase() {
        PomodoroService.skip(application)
    }

    /**
     * Toggles between pause and resume.
     */
    fun togglePauseResume() {
        if (_pomodoroState.value.isPaused) {
            resumeTimer()
        } else {
            pauseTimer()
        }
    }

    /**
     * Updates Pomodoro settings.
     */
    fun updateSettings(settings: PomodoroSettings) {
        viewModelScope.launch {
            preferencesManager.setPomodoroWorkDuration(settings.workDurationMinutes)
            preferencesManager.setPomodoroShortBreak(settings.shortBreakMinutes)
            preferencesManager.setPomodoroLongBreak(settings.longBreakMinutes)
            preferencesManager.setPomodorosUntilLongBreak(settings.pomodorosUntilLongBreak)
            preferencesManager.setPomodoroAutoStartBreaks(settings.autoStartBreaks)
            preferencesManager.setPomodoroAutoStartPomodoros(settings.autoStartPomodoros)
        }
    }

    /**
     * Updates a specific setting.
     */
    fun updateWorkDuration(minutes: Int) {
        viewModelScope.launch {
            preferencesManager.setPomodoroWorkDuration(minutes)
        }
    }

    fun updateShortBreak(minutes: Int) {
        viewModelScope.launch {
            preferencesManager.setPomodoroShortBreak(minutes)
        }
    }

    fun updateLongBreak(minutes: Int) {
        viewModelScope.launch {
            preferencesManager.setPomodoroLongBreak(minutes)
        }
    }

    fun updatePomodorosUntilLongBreak(count: Int) {
        viewModelScope.launch {
            preferencesManager.setPomodorosUntilLongBreak(count)
        }
    }

    fun updateAutoStartBreaks(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setPomodoroAutoStartBreaks(enabled)
        }
    }

    fun updateAutoStartPomodoros(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setPomodoroAutoStartPomodoros(enabled)
        }
    }
}
