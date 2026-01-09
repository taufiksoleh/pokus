package com.tscorp.pokus.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tscorp.pokus.data.repository.AppRepository
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
 * ViewModel for the Settings screen.
 * Manages app preferences and settings.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val appRepository: AppRepository
) : AndroidViewModel(application) {

    /**
     * UI state for the settings screen.
     */
    data class SettingsUiState(
        val showSystemApps: Boolean = false,
        val blockedAppsCount: Int = 0,
        val showClearDataDialog: Boolean = false,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * Show system apps preference.
     */
    val showSystemApps: StateFlow<Boolean> = appRepository.showSystemApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Blocked apps count.
     */
    val blockedAppsCount: StateFlow<Int> = appRepository.blockedAppsCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        viewModelScope.launch {
            combine(
                showSystemApps,
                blockedAppsCount
            ) { showSystem, count ->
                SettingsUiState(
                    showSystemApps = showSystem,
                    blockedAppsCount = count,
                    showClearDataDialog = _uiState.value.showClearDataDialog,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Toggle show system apps preference.
     */
    fun toggleShowSystemApps() {
        viewModelScope.launch {
            val currentValue = showSystemApps.value
            appRepository.setShowSystemApps(!currentValue)
        }
    }

    /**
     * Show the clear data confirmation dialog.
     */
    fun showClearDataDialog() {
        _uiState.value = _uiState.value.copy(showClearDataDialog = true)
    }

    /**
     * Hide the clear data confirmation dialog.
     */
    fun hideClearDataDialog() {
        _uiState.value = _uiState.value.copy(showClearDataDialog = false)
    }

    /**
     * Clear all blocked apps data.
     */
    fun clearAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Disable focus mode first
                appRepository.setFocusModeEnabled(false)

                // Clear all blocked apps
                appRepository.clearAllBlockedApps()

                _uiState.value = _uiState.value.copy(
                    showClearDataDialog = false,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showClearDataDialog = false,
                    isLoading = false
                )
            }
        }
    }
}
