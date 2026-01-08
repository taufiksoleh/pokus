package com.tscorp.pokus.util

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tscorp.pokus.PokusApplication
import com.tscorp.pokus.R

/**
 * Helper object for creating and managing notifications.
 */
object NotificationHelper {

    private const val BLOCKED_APP_NOTIFICATION_ID = 2001

    /**
     * Shows a notification when a blocked app is accessed.
     *
     * @param context Application context
     * @param appName Name of the blocked app
     */
    fun showBlockedAppNotification(context: Context, appName: String) {
        if (!hasNotificationPermission(context)) {
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        val notification = NotificationCompat.Builder(context, PokusApplication.CHANNEL_BLOCK_ALERT)
            .setContentTitle("App Blocked")
            .setContentText("$appName was blocked. Stay focused!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(BLOCKED_APP_NOTIFICATION_ID, notification)
    }

    /**
     * Cancels the blocked app notification.
     *
     * @param context Application context
     */
    fun cancelBlockedAppNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        notificationManager.cancel(BLOCKED_APP_NOTIFICATION_ID)
    }

    /**
     * Checks if the app has notification permission.
     *
     * @param context Application context
     * @return True if notification permission is granted
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Before Android 13, notification permission is granted by default
            true
        }
    }

    /**
     * Checks if notification channel is enabled.
     *
     * @param context Application context
     * @param channelId The notification channel ID to check
     * @return True if the channel is enabled
     */
    fun isChannelEnabled(context: Context, channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return true
    }
}
