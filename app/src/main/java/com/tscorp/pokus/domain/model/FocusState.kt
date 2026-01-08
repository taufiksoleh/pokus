package com.tscorp.pokus.domain.model

/**
 * Represents the current state of focus mode in the application.
 *
 * @property isEnabled Whether focus mode is currently active
 * @property startTime The timestamp when focus mode was enabled (0 if not active)
 * @property blockedAppsCount The number of apps currently being blocked
 */
data class FocusState(
    val isEnabled: Boolean = false,
    val startTime: Long = 0L,
    val blockedAppsCount: Int = 0
) {
    /**
     * Calculate the duration of the current focus session in milliseconds.
     * Returns 0 if focus mode is not enabled or start time is not set.
     */
    val focusDurationMillis: Long
        get() = if (isEnabled && startTime > 0) {
            System.currentTimeMillis() - startTime
        } else {
            0L
        }

    /**
     * Get formatted focus duration as a human-readable string (e.g., "2h 30m").
     */
    val formattedDuration: String
        get() {
            val totalMinutes = focusDurationMillis / 1000 / 60
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "0m"
            }
        }

    companion object {
        /**
         * Default inactive focus state.
         */
        val Inactive = FocusState(
            isEnabled = false,
            startTime = 0L,
            blockedAppsCount = 0
        )
    }
}
