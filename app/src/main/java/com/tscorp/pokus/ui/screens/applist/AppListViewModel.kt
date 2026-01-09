package com.tscorp.pokus.ui.screens.applist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tscorp.pokus.data.repository.AppRepository
import com.tscorp.pokus.domain.model.InstalledApp
import com.tscorp.pokus.domain.usecase.GetInstalledAppsUseCase
import com.tscorp.pokus.service.AppMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the App List screen.
 * Manages the list of installed apps and blocking/unblocking operations.
 */
@HiltViewModel
class AppListViewModel @Inject constructor(
    private val application: Application,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val appRepository: AppRepository
) : AndroidViewModel(application) {

    /**
     * UI state for the app list screen.
     */
    data class AppListUiState(
        val apps: List<InstalledApp> = emptyList(),
        val filteredApps: List<InstalledApp> = emptyList(),
        val searchQuery: String = "",
        val showSystemApps: Boolean = false,
        val isLoading: Boolean = true,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    /**
     * Show system apps preference from repository.
     */
    val showSystemApps: StateFlow<Boolean> = appRepository.showSystemApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        loadApps()
    }

    /**
     * Load installed apps from the device.
     */
    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val includeSystemApps = appRepository.showSystemApps.first()
                val apps = getInstalledAppsUseCase(includeSystemApps)

                _uiState.value = _uiState.value.copy(
                    apps = apps,
                    filteredApps = filterApps(apps, _uiState.value.searchQuery),
                    showSystemApps = includeSystemApps,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load apps"
                )
            }
        }
    }

    /**
     * Search apps by query.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredApps = filterApps(_uiState.value.apps, query)
        )
    }

    /**
     * Filter apps based on search query.
     */
    private fun filterApps(apps: List<InstalledApp>, query: String): List<InstalledApp> {
        if (query.isBlank()) return apps

        return apps.filter { app ->
            app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
        }
    }

    /**
     * Toggle the blocked status of an app.
     */
    fun toggleAppBlocked(app: InstalledApp) {
        viewModelScope.launch {
            try {
                if (app.isBlocked) {
                    // Unblock the app
                    appRepository.unblockApp(app.packageName)
                } else {
                    // Block the app
                    appRepository.blockApp(app.packageName, app.appName)
                }

                // Refresh the app list to reflect changes
                loadApps()

                // Notify service to refresh blocked apps if focus mode is enabled
                refreshServiceIfNeeded()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update app status"
                )
            }
        }
    }

    /**
     * Toggle show system apps preference.
     */
    fun toggleShowSystemApps() {
        viewModelScope.launch {
            val currentValue = appRepository.showSystemApps.first()
            appRepository.setShowSystemApps(!currentValue)
            loadApps()
        }
    }

    /**
     * Clear the current error.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Notifies the service to refresh its blocked apps list if focus mode is enabled.
     */
    private suspend fun refreshServiceIfNeeded() {
        val isFocusModeEnabled = appRepository.isFocusModeEnabled.first()
        if (isFocusModeEnabled) {
            val intent = AppMonitorService.refreshBlockedAppsIntent(application)
            application.startService(intent)
        }
    }
}
