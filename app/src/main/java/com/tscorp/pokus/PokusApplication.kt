package com.tscorp.pokus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for Pokus app.
 * Initializes Hilt dependency injection and creates notification channels.
 */
@HiltAndroidApp
class PokusApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Creates notification channels required for the app.
     * Must be called before posting any notifications.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Channel for focus mode service
            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS_SERVICE,
                "Focus Mode",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when focus mode is active"
                setShowBadge(false)
            }

            // Channel for blocked app alerts
            val alertChannel = NotificationChannel(
                CHANNEL_BLOCK_ALERT,
                "Block Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a blocked app is accessed"
                enableVibration(true)
            }

            // Channel for Pomodoro timer
            val pomodoroChannel = NotificationChannel(
                CHANNEL_POMODORO,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows Pomodoro timer progress"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(
                listOf(focusChannel, alertChannel, pomodoroChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_FOCUS_SERVICE = "focus_service_channel"
        const val CHANNEL_BLOCK_ALERT = "block_alert_channel"
        const val CHANNEL_POMODORO = "pomodoro_channel"
    }
}
