package com.tscorp.pokus.domain.usecase

import android.content.Context
import com.tscorp.pokus.data.repository.AppRepository
import com.tscorp.pokus.domain.model.InstalledApp
import com.tscorp.pokus.util.AppUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for retrieving installed applications.
 * Combines data from PackageManager with blocked status from repository.
 */
@Singleton
class GetInstalledAppsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appRepository: AppRepository
) {
    /**
     * Get all installed apps with their blocked status.
     *
     * @param includeSystemApps Whether to include system apps
     * @return List of InstalledApp objects sorted alphabetically by name
     */
    suspend operator fun invoke(includeSystemApps: Boolean = false): List<InstalledApp> {
        return withContext(Dispatchers.IO) {
            // Get blocked package names from repository
            val blockedPackages = appRepository.getBlockedPackageNames().toSet()

            // Get installed apps with blocked status
            AppUtils.getInstalledApps(
                context = context,
                includeSystemApps = includeSystemApps,
                blockedPackages = blockedPackages
            )
        }
    }

    /**
     * Get only apps that are currently blocked.
     *
     * @return List of InstalledApp objects that are blocked
     */
    suspend fun getBlockedApps(): List<InstalledApp> {
        return withContext(Dispatchers.IO) {
            val blockedApps = appRepository.activeBlockedApps.first()
            val blockedPackages = blockedApps.map { it.packageName }.toSet()

            AppUtils.getInstalledApps(
                context = context,
                includeSystemApps = true,
                blockedPackages = blockedPackages
            ).filter { it.isBlocked }
        }
    }

    /**
     * Search installed apps by name.
     *
     * @param query Search query
     * @param includeSystemApps Whether to include system apps
     * @return Filtered list of InstalledApp objects
     */
    suspend fun search(query: String, includeSystemApps: Boolean = false): List<InstalledApp> {
        if (query.isBlank()) {
            return invoke(includeSystemApps)
        }

        return invoke(includeSystemApps).filter { app ->
            app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
        }
    }
}
