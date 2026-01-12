package com.tscorp.pokus.domain.model

/**
 * Represents the state of the Pomodoro timer.
 */
enum class PomodoroPhase {
    WORK,           // Work session (default 25 minutes)
    SHORT_BREAK,    // Short break (default 5 minutes)
    LONG_BREAK,     // Long break after 4 pomodoros (default 15 minutes)
    IDLE            // Timer not running
}

/**
 * Represents the current state of the Pomodoro timer.
 *
 * @property isRunning Whether the timer is currently running
 * @property isPaused Whether the timer is paused
 * @property phase The current phase of the Pomodoro timer
 * @property remainingTimeMillis The remaining time in milliseconds
 * @property totalTimeMillis The total time for the current phase in milliseconds
 * @property completedPomodoros The number of completed work sessions in the current cycle
 */
data class PomodoroState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val phase: PomodoroPhase = PomodoroPhase.IDLE,
    val remainingTimeMillis: Long = 0L,
    val totalTimeMillis: Long = 0L,
    val completedPomodoros: Int = 0
) {
    /**
     * Get the progress percentage (0.0 to 1.0) of the current timer.
     */
    val progress: Float
        get() = if (totalTimeMillis > 0) {
            ((totalTimeMillis - remainingTimeMillis).toFloat() / totalTimeMillis.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    /**
     * Get formatted remaining time as MM:SS string.
     */
    val formattedTime: String
        get() {
            val totalSeconds = (remainingTimeMillis / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    /**
     * Get the display name for the current phase.
     */
    val phaseDisplayName: String
        get() = when (phase) {
            PomodoroPhase.WORK -> "Focus Time"
            PomodoroPhase.SHORT_BREAK -> "Short Break"
            PomodoroPhase.LONG_BREAK -> "Long Break"
            PomodoroPhase.IDLE -> "Ready"
        }

    companion object {
        /**
         * Default idle state.
         */
        val Idle = PomodoroState(
            isRunning = false,
            isPaused = false,
            phase = PomodoroPhase.IDLE,
            remainingTimeMillis = 0L,
            totalTimeMillis = 0L,
            completedPomodoros = 0
        )
    }
}
