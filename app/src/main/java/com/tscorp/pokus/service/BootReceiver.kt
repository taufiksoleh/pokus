package com.tscorp.pokus.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tscorp.pokus.data.preferences.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that restarts the AppMonitorService
 * when the device boots up, if focus mode was previously enabled.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Use goAsync() to extend the time we have to complete the work
            val pendingResult = goAsync()

            scope.launch {
                try {
                    restartServiceIfNeeded(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    /**
     * Restarts the monitoring service if focus mode was previously enabled.
     */
    private suspend fun restartServiceIfNeeded(context: Context) {
        // Check if focus mode was enabled before reboot
        val isFocusModeEnabled = preferencesManager.isFocusModeEnabled.first()

        if (isFocusModeEnabled) {
            startMonitoringService(context)
        }
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
