package com.tscorp.pokus.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.tscorp.pokus.data.repository.AppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NotificationListenerService that filters notifications from blocked apps.
 * When an app is blocked, its notifications will be automatically dismissed.
 */
@AndroidEntryPoint
class NotificationFilterService : NotificationListenerService() {

    @Inject
    lateinit var appRepository: AppRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var blockedPackages: Set<String> = emptySet()

    companion object {
        private const val TAG = "NotificationFilterService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationFilterService created")

        // Load blocked packages when service starts
        serviceScope.launch {
            try {
                blockedPackages = appRepository.getBlockedPackageNames().first().toSet()
                Log.d(TAG, "Loaded ${blockedPackages.size} blocked packages")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading blocked packages", e)
            }
        }

        // Listen for changes to blocked apps
        serviceScope.launch {
            appRepository.getBlockedPackageNames().collect { packages ->
                blockedPackages = packages.toSet()
                Log.d(TAG, "Updated blocked packages: ${blockedPackages.size} apps")

                // Check and dismiss any existing notifications from newly blocked apps
                checkAndDismissBlockedNotifications()
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val packageName = it.packageName

            // Check if the notification is from a blocked app
            if (blockedPackages.contains(packageName)) {
                Log.d(TAG, "Dismissing notification from blocked app: $packageName")
                cancelNotification(it.key)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService connected")

        // Dismiss any existing notifications from blocked apps
        checkAndDismissBlockedNotifications()
    }

    /**
     * Check all active notifications and dismiss those from blocked apps.
     */
    private fun checkAndDismissBlockedNotifications() {
        try {
            val activeNotifications = activeNotifications ?: return

            for (notification in activeNotifications) {
                if (blockedPackages.contains(notification.packageName)) {
                    Log.d(TAG, "Dismissing existing notification from blocked app: ${notification.packageName}")
                    cancelNotification(notification.key)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking active notifications", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NotificationFilterService destroyed")
        serviceScope.cancel()
    }
}
