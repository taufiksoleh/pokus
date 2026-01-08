package com.tscorp.pokus.data.repository

import com.tscorp.pokus.data.local.dao.BlockedAppDao
import com.tscorp.pokus.data.local.entity.BlockedApp
import com.tscorp.pokus.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing app data.
 * Acts as a single source of truth for blocked apps and focus mode state.
 */
@Singleton
class AppRepository @Inject constructor(
    private val blockedAppDao: BlockedAppDao,
    private val preferencesManager: PreferencesManager
) {
    // ==================== Blocked Apps ====================

    /**
     * Get all blocked apps as a Flow.
     */
    val allBlockedApps: Flow<List<BlockedApp>> = blockedAppDao.getAllBlockedApps()

    /**
     * Get only active (currently blocked) apps.
     */
    val activeBlockedApps: Flow<List<BlockedApp>> = blockedAppDao.getActiveBlockedApps()

    /**
     * Get the count of blocked apps.
     */
    val blockedAppsCount: Flow<Int> = blockedAppDao.getBlockedAppsCount()

    /**
     * Add an app to the blocked list.
     */
    suspend fun blockApp(packageName: String, appName: String) {
        val blockedApp = BlockedApp(
            packageName = packageName,
            appName = appName,
            isBlocked = true
        )
        blockedAppDao.insertBlockedApp(blockedApp)
    }

    /**
     * Add multiple apps to the blocked list.
     */
    suspend fun blockApps(apps: List<Pair<String, String>>) {
        val blockedApps = apps.map { (packageName, appName) ->
            BlockedApp(
                packageName = packageName,
                appName = appName,
                isBlocked = true
            )
        }
        blockedAppDao.insertBlockedApps(blockedApps)
    }

    /**
     * Remove an app from the blocked list.
     */
    suspend fun unblockApp(packageName: String) {
        blockedAppDao.deleteByPackageName(packageName)
    }

    /**
     * Toggle the blocked status of an app.
     */
    suspend fun toggleAppBlockedStatus(packageName: String, isBlocked: Boolean) {
        blockedAppDao.updateBlockedStatus(packageName, isBlocked)
    }

    /**
     * Check if an app is currently blocked.
     */
    suspend fun isAppBlocked(packageName: String): Boolean {
        return blockedAppDao.isAppBlocked(packageName)
    }

    /**
     * Get a blocked app by its package name.
     */
    suspend fun getBlockedApp(packageName: String): BlockedApp? {
        return blockedAppDao.getBlockedAppByPackage(packageName)
    }

    /**
     * Get all blocked package names (for use in the monitoring service).
     */
    suspend fun getBlockedPackageNames(): List<String> {
        return blockedAppDao.getBlockedPackageNames()
    }

    /**
     * Remove all blocked apps.
     */
    suspend fun clearAllBlockedApps() {
        blockedAppDao.deleteAllBlockedApps()
    }

    // ==================== Focus Mode ====================

    /**
     * Get focus mode enabled state as a Flow.
     */
    val isFocusModeEnabled: Flow<Boolean> = preferencesManager.isFocusModeEnabled

    /**
     * Get focus mode start time as a Flow.
     */
    val focusModeStartTime: Flow<Long> = preferencesManager.focusModeStartTime

    /**
     * Enable or disable focus mode.
     */
    suspend fun setFocusModeEnabled(enabled: Boolean) {
        preferencesManager.setFocusModeEnabled(enabled)
    }

    // ==================== Onboarding ====================

    /**
     * Get onboarding completed state as a Flow.
     */
    val isOnboardingCompleted: Flow<Boolean> = preferencesManager.isOnboardingCompleted

    /**
     * Mark onboarding as completed.
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesManager.setOnboardingCompleted(completed)
    }

    // ==================== Settings ====================

    /**
     * Get show system apps preference as a Flow.
     */
    val showSystemApps: Flow<Boolean> = preferencesManager.showSystemApps

    /**
     * Set whether to show system apps in the app list.
     */
    suspend fun setShowSystemApps(show: Boolean) {
        preferencesManager.setShowSystemApps(show)
    }
}
