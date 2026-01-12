package com.tscorp.pokus.ui.screens.home

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tscorp.pokus.data.local.entity.BlockedApp
import com.tscorp.pokus.data.repository.AppRepository
import com.tscorp.pokus.domain.model.FocusState
import com.tscorp.pokus.service.AppMonitorService
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
 * ViewModel for the Home screen.
 * Manages focus mode state, blocked apps display, and service control.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val appRepository: AppRepository
) : AndroidViewModel(application) {

    /**
     * UI state for the home screen.
     */
    data class HomeUiState(
        val focusState: FocusState = FocusState.Inactive,
        val blockedApps: List<BlockedApp> = emptyList(),
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Combined flow of focus state from repository.
     */
    val focusState: StateFlow<FocusState> = combine(
        appRepository.isFocusModeEnabled,
        appRepository.focusModeStartTime,
        appRepository.blockedAppsCount
    ) { isEnabled, startTime, blockedCount ->
        FocusState(
            isEnabled = isEnabled,
            startTime = startTime,
            blockedAppsCount = blockedCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusState.Inactive
    )

    /**
     * Blocked apps from repository.
     */
    val blockedApps: StateFlow<List<BlockedApp>> = appRepository.activeBlockedApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            combine(
                focusState,
                blockedApps
            ) { focus, apps ->
                HomeUiState(
                    focusState = focus,
                    blockedApps = apps,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        // Automatically start/stop monitoring service when focus mode state changes
        viewModelScope.launch {
            var previousFocusState = false
            focusState.collect { state ->
                if (state.isEnabled != previousFocusState) {
                    if (state.isEnabled) {
                        startMonitoringService()
                    } else {
                        stopMonitoringService()
                    }
                    previousFocusState = state.isEnabled
                }
            }
        }
    }

    /**
     * Toggle focus mode on/off.
     */
    fun toggleFocusMode() {
        viewModelScope.launch {
            val currentState = focusState.value.isEnabled
            val newState = !currentState

            // Update repository
            appRepository.setFocusModeEnabled(newState)

            // Start or stop the monitoring service
            if (newState) {
                startMonitoringService()
            } else {
                stopMonitoringService()
            }
        }
    }

    /**
     * Enable focus mode.
     */
    fun enableFocusMode() {
        viewModelScope.launch {
            appRepository.setFocusModeEnabled(true)
            startMonitoringService()
        }
    }

    /**
     * Disable focus mode.
     */
    fun disableFocusMode() {
        viewModelScope.launch {
            appRepository.setFocusModeEnabled(false)
            stopMonitoringService()
        }
    }

    /**
     * Remove an app from the blocked list.
     */
    fun unblockApp(packageName: String) {
        viewModelScope.launch {
            appRepository.unblockApp(packageName)
            // Notify service to refresh blocked apps
            refreshService()
        }
    }

    /**
     * Starts the app monitoring foreground service.
     */
    private fun startMonitoringService() {
        val intent = AppMonitorService.startIntent(application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(intent)
        } else {
            application.startService(intent)
        }
    }

    /**
     * Stops the app monitoring foreground service.
     */
    private fun stopMonitoringService() {
        val intent = AppMonitorService.stopIntent(application)
        application.startService(intent)
    }

    /**
     * Notifies the service to refresh its blocked apps list.
     */
    private fun refreshService() {
        if (focusState.value.isEnabled) {
            val intent = AppMonitorService.refreshBlockedAppsIntent(application)
            application.startService(intent)
        }
    }
}
