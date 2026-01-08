package com.tscorp.pokus.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tscorp.pokus.MainActivity
import com.tscorp.pokus.PokusApplication
import com.tscorp.pokus.R
import com.tscorp.pokus.data.repository.AppRepository
import com.tscorp.pokus.ui.overlay.BlockOverlayActivity
import com.tscorp.pokus.util.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that monitors the currently running app
 * and blocks access to apps in the blocked list.
 *
 * This service runs continuously when focus mode is enabled,
 * checking the foreground app and showing the block overlay
 * when a blocked app is detected.
 */
@AndroidEntryPoint
class AppMonitorService : Service() {

    @Inject
    lateinit var appRepository: AppRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null

    // Cache of blocked package names for faster lookup
    private var blockedPackages: Set<String> = emptySet()

    // Track the last blocked package to avoid showing overlay repeatedly
    private var lastBlockedPackage: String? = null

    // Polling interval in milliseconds
    private val pollingInterval = 500L

    override fun onCreate() {
        super.onCreate()
        // Load blocked packages initially
        serviceScope.launch {
            refreshBlockedPackages()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
            ACTION_REFRESH_BLOCKED_APPS -> {
                serviceScope.launch {
                    refreshBlockedPackages()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        serviceScope.cancel()
    }

    /**
     * Refreshes the cached list of blocked package names from the repository.
     */
    private suspend fun refreshBlockedPackages() {
        val packages = appRepository.getBlockedPackageNames()
        blockedPackages = packages.toSet()
    }

    /**
     * Starts the foreground service and begins monitoring apps.
     */
    private fun startMonitoring() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Cancel any existing monitoring job
        monitoringJob?.cancel()

        // Start the polling loop
        monitoringJob = serviceScope.launch {
            // Refresh blocked packages before starting
            refreshBlockedPackages()

            while (isActive) {
                checkForegroundApp()
                delay(pollingInterval)
            }
        }
    }

    /**
     * Checks the current foreground app and shows overlay if it's blocked.
     */
    private suspend fun checkForegroundApp() {
        val currentForegroundApp = AppUtils.getCurrentForegroundApp(this)

        if (currentForegroundApp != null) {
            // Skip if it's our own app or the overlay
            if (currentForegroundApp == packageName) {
                lastBlockedPackage = null
                return
            }

            // Check if the current app is in the blocked list
            if (blockedPackages.contains(currentForegroundApp)) {
                // Only show overlay if this is a new blocked app access
                if (lastBlockedPackage != currentForegroundApp) {
                    lastBlockedPackage = currentForegroundApp
                    showBlockOverlay(currentForegroundApp)
                }
            } else {
                // Reset last blocked package when user is on a non-blocked app
                lastBlockedPackage = null
            }
        }
    }

    /**
     * Shows the block overlay for a blocked app.
     */
    private fun showBlockOverlay(packageName: String) {
        val appName = AppUtils.getAppName(this, packageName)

        val overlayIntent = BlockOverlayActivity.createIntent(
            context = this,
            appName = appName,
            packageName = packageName
        )

        startActivity(overlayIntent)
    }

    /**
     * Stops the monitoring service.
     */
    private fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        lastBlockedPackage = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Creates the persistent notification for the foreground service.
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            stopIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, PokusApplication.CHANNEL_FOCUS_SERVICE)
            .setContentTitle("Focus Mode Active")
            .setContentText("Blocking distracting apps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Stop",
                stopIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        const val ACTION_START = "com.tscorp.pokus.action.START_MONITORING"
        const val ACTION_STOP = "com.tscorp.pokus.action.STOP_MONITORING"
        const val ACTION_REFRESH_BLOCKED_APPS = "com.tscorp.pokus.action.REFRESH_BLOCKED_APPS"
        const val NOTIFICATION_ID = 1001

        /**
         * Creates an intent to start the monitoring service.
         */
        fun startIntent(context: Context): Intent {
            return Intent(context, AppMonitorService::class.java).apply {
                action = ACTION_START
            }
        }

        /**
         * Creates an intent to stop the monitoring service.
         */
        fun stopIntent(context: Context): Intent {
            return Intent(context, AppMonitorService::class.java).apply {
                action = ACTION_STOP
            }
        }

        /**
         * Creates an intent to refresh the blocked apps list.
         */
        fun refreshBlockedAppsIntent(context: Context): Intent {
            return Intent(context, AppMonitorService::class.java).apply {
                action = ACTION_REFRESH_BLOCKED_APPS
            }
        }
    }
}
