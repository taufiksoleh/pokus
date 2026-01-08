package com.tscorp.pokus.ui

/**
 * Sealed interface representing one-time UI events.
 * These events are consumed once and should not persist across configuration changes.
 */
sealed interface UiEvent {

    /**
     * Navigate to a specific route.
     */
    data class Navigate(val route: String) : UiEvent

    /**
     * Navigate back to previous screen.
     */
    data object NavigateBack : UiEvent

    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null
    ) : UiEvent

    /**
     * Show a toast message.
     */
    data class ShowToast(val message: String) : UiEvent

    /**
     * Open system settings for specific action.
     */
    data class OpenSettings(val action: String) : UiEvent
}
