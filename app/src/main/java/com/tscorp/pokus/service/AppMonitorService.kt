package com.tscorp.pokus.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tscorp.pokus.MainActivity
import com.tscorp.pokus.PokusApplication
import com.tscorp.pokus.R
import dagger.hilt.android.AndroidEntryPoint

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

    override fun onCreate() {
        super.onCreate()
        // Service initialization will be implemented in Phase 4
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }

    /**
     * Starts the foreground service and begins monitoring apps.
     */
    private fun startMonitoring() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        // App monitoring logic will be implemented in Phase 4
    }

    /**
     * Stops the monitoring service.
     */
    private fun stopMonitoring() {
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

        return NotificationCompat.Builder(this, PokusApplication.CHANNEL_FOCUS_SERVICE)
            .setContentTitle("Focus Mode Active")
            .setContentText("Blocking distracting apps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        const val ACTION_START = "com.tscorp.pokus.action.START_MONITORING"
        const val ACTION_STOP = "com.tscorp.pokus.action.STOP_MONITORING"
        const val NOTIFICATION_ID = 1001

        /**
         * Creates an intent to start the monitoring service.
         */
        fun startIntent(context: android.content.Context): Intent {
            return Intent(context, AppMonitorService::class.java).apply {
                action = ACTION_START
            }
        }

        /**
         * Creates an intent to stop the monitoring service.
         */
        fun stopIntent(context: android.content.Context): Intent {
            return Intent(context, AppMonitorService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }
}
