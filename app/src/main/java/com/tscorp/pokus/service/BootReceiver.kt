package com.tscorp.pokus.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver that restarts the AppMonitorService
 * when the device boots up, if focus mode was previously enabled.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    // TODO: Inject preferences manager in Phase 4 to check if focus mode was enabled
    // @Inject
    // lateinit var preferencesManager: PreferencesManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Check if focus mode was enabled before reboot
            // This will be implemented in Phase 4 when we have the preferences manager
            restartServiceIfNeeded(context)
        }
    }

    /**
     * Restarts the monitoring service if focus mode was previously enabled.
     */
    private fun restartServiceIfNeeded(context: Context) {
        // TODO: Check preferences if focus mode was enabled
        // For now, this is a stub that will be implemented in Phase 4
        //
        // Example implementation:
        // if (preferencesManager.isFocusModeEnabled()) {
        //     startMonitoringService(context)
        // }
    }

    /**
     * Starts the monitoring service with proper API level handling.
     */
    private fun startMonitoringService(context: Context) {
        val serviceIntent = AppMonitorService.startIntent(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
