package com.tscorp.pokus.util

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.tscorp.pokus.domain.model.InstalledApp

/**
 * Utility object for app-related operations.
 */
object AppUtils {

    /**
     * Get the current foreground app package name using UsageStatsManager.
     *
     * @param context Application context
     * @return Package name of the current foreground app, or null if unavailable
     */
    fun getCurrentForegroundApp(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
            as? UsageStatsManager ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 // Last minute

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endTime
        )

        return usageStatsList
            ?.filter { it.lastTimeUsed > 0 }
            ?.maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }

    /**
     * Get all installed apps on the device.
     *
     * @param context Application context
     * @param includeSystemApps Whether to include system apps in the list
     * @param blockedPackages Set of package names that are currently blocked
     * @return List of InstalledApp objects
     */
    fun getInstalledApps(
        context: Context,
        includeSystemApps: Boolean = false,
        blockedPackages: Set<String> = emptySet()
    ): List<InstalledApp> {
        val packageManager = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }

        return resolveInfoList
            .mapNotNull { resolveInfo ->
                val appInfo = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                val packageName = appInfo.packageName

                // Skip our own app
                if (packageName == context.packageName) return@mapNotNull null

                val isSystemApp = isSystemApp(appInfo)

                // Filter system apps if not included
                if (!includeSystemApps && isSystemApp) return@mapNotNull null

                InstalledApp(
                    packageName = packageName,
                    appName = appInfo.loadLabel(packageManager).toString(),
                    icon = getAppIcon(packageManager, packageName),
                    isSystemApp = isSystemApp,
                    isBlocked = blockedPackages.contains(packageName)
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(compareByDescending<InstalledApp> { it.isBlocked }.thenBy { it.appName.lowercase() })
    }

    /**
     * Check if an ApplicationInfo represents a system app.
     */
    fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    /**
     * Get the app icon for a given package name.
     *
     * @param packageManager PackageManager instance
     * @param packageName Package name of the app
     * @return App icon drawable or null if unavailable
     */
    fun getAppIcon(packageManager: PackageManager, packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        } catch (e: Exception) {
            // Handle any other exceptions (e.g., SecurityException)
            null
        }
    }

    /**
     * Get the app icon for a given package name using Context.
     *
     * @param context Application context
     * @param packageName Package name of the app
     * @return App icon drawable or null if unavailable
     */
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return getAppIcon(context.packageManager, packageName)
    }

    /**
     * Get the app name for a given package name.
     *
     * @param context Application context
     * @param packageName Package name of the app
     * @return App name or package name if unavailable
     */
    fun getAppName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            appInfo.loadLabel(packageManager).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    /**
     * Check if a package exists on the device.
     *
     * @param context Application context
     * @param packageName Package name to check
     * @return True if the package exists
     */
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Common social media and distraction app package names.
     * Useful for suggesting apps to block.
     */
    val COMMON_DISTRACTION_APPS = listOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.twitter.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.google.android.youtube",
        "com.snapchat.android",
        "com.reddit.frontpage",
        "com.pinterest",
        "com.linkedin.android",
        "com.discord",
        "com.whatsapp",
        "org.telegram.messenger"
    )
}
