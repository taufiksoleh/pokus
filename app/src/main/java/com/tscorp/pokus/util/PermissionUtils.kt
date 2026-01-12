package com.tscorp.pokus.util

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Utility object for checking and requesting permissions required by the app.
 */
object PermissionUtils {

    /**
     * Checks if the app has Usage Stats permission.
     * This permission is required to detect the current foreground app.
     *
     * @param context Application context
     * @return True if usage stats permission is granted
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }

        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Checks if the app has overlay (draw over other apps) permission.
     * This permission is required to show the blocking overlay.
     *
     * @param context Application context
     * @return True if overlay permission is granted
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Checks if the app has notification permission.
     * This is required on Android 13+ to post notifications.
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
            true
        }
    }

    /**
     * Checks if all required permissions are granted.
     *
     * @param context Application context
     * @return True if all required permissions are granted
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return hasUsageStatsPermission(context) &&
                hasOverlayPermission(context) &&
                hasNotificationPermission(context)
    }

    /**
     * Creates an intent to open Usage Access settings.
     *
     * @return Intent to open Usage Access settings
     */
    fun usageStatsSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    /**
     * Creates an intent to open Overlay permission settings for the app.
     *
     * @param context Application context
     * @return Intent to open Overlay settings
     */
    fun overlaySettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    /**
     * Creates an intent to open App notification settings.
     *
     * @param context Application context
     * @return Intent to open notification settings
     */
    fun notificationSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    /**
     * Creates an intent to open the app details settings page.
     *
     * @param context Application context
     * @return Intent to open app details settings
     */
    fun appDetailsSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    /**
     * Represents the permission status for all required permissions.
     */
    data class PermissionStatus(
        val hasUsageStats: Boolean,
        val hasOverlay: Boolean,
        val hasNotification: Boolean
    ) {
        val allGranted: Boolean
            get() = hasUsageStats && hasOverlay && hasNotification

        val grantedCount: Int
            get() = listOf(hasUsageStats, hasOverlay, hasNotification).count { it }

        val totalCount: Int = 3
    }

    /**
     * Gets the current status of all required permissions.
     *
     * @param context Application context
     * @return PermissionStatus object with current permission states
     */
    fun getPermissionStatus(context: Context): PermissionStatus {
        return PermissionStatus(
            hasUsageStats = hasUsageStatsPermission(context),
            hasOverlay = hasOverlayPermission(context),
            hasNotification = hasNotificationPermission(context)
        )
    }
}
