package com.tscorp.pokus.domain.model

/**
 * Settings for the Pomodoro timer.
 *
 * @property workDurationMinutes Duration of work sessions in minutes (default: 25)
 * @property shortBreakMinutes Duration of short breaks in minutes (default: 5)
 * @property longBreakMinutes Duration of long breaks in minutes (default: 15)
 * @property pomodorosUntilLongBreak Number of pomodoros before a long break (default: 4)
 * @property autoStartBreaks Whether to automatically start breaks after work sessions
 * @property autoStartPomodoros Whether to automatically start work sessions after breaks
 */
data class PomodoroSettings(
    val workDurationMinutes: Int = DEFAULT_WORK_DURATION,
    val shortBreakMinutes: Int = DEFAULT_SHORT_BREAK,
    val longBreakMinutes: Int = DEFAULT_LONG_BREAK,
    val pomodorosUntilLongBreak: Int = DEFAULT_POMODOROS_UNTIL_LONG_BREAK,
    val autoStartBreaks: Boolean = false,
    val autoStartPomodoros: Boolean = false
) {
    /**
     * Get work duration in milliseconds.
     */
    val workDurationMillis: Long
        get() = workDurationMinutes * 60 * 1000L

    /**
     * Get short break duration in milliseconds.
     */
    val shortBreakMillis: Long
        get() = shortBreakMinutes * 60 * 1000L

    /**
     * Get long break duration in milliseconds.
     */
    val longBreakMillis: Long
        get() = longBreakMinutes * 60 * 1000L

    companion object {
        const val DEFAULT_WORK_DURATION = 25
        const val DEFAULT_SHORT_BREAK = 5
        const val DEFAULT_LONG_BREAK = 15
        const val DEFAULT_POMODOROS_UNTIL_LONG_BREAK = 4

        const val MIN_DURATION = 1
        const val MAX_DURATION = 120

        /**
         * Default Pomodoro settings.
         */
        val Default = PomodoroSettings()
    }
}
