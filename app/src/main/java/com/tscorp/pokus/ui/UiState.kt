package com.tscorp.pokus.ui

/**
 * Sealed interface representing common UI states.
 * Use this for composable screens to handle loading, success, and error states.
 *
 * @param T The type of data on success state
 */
sealed interface UiState<out T> {

    /**
     * Initial state before any data is loaded.
     */
    data object Idle : UiState<Nothing>

    /**
     * Loading state while fetching data.
     */
    data object Loading : UiState<Nothing>

    /**
     * Success state with data.
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * Error state with optional message.
     */
    data class Error(val message: String? = null) : UiState<Nothing>
}

/**
 * Extension function to get data if state is Success, null otherwise.
 */
fun <T> UiState<T>.getOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}

/**
 * Extension function to check if state is loading.
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Extension function to check if state is success.
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Extension function to check if state is error.
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error
