package com.tscorp.pokus.domain.model

import android.graphics.drawable.Drawable

/**
 * Model representing an installed application on the device.
 *
 * @property packageName The unique package name of the app (e.g., "com.instagram.android")
 * @property appName The display name of the app
 * @property icon The app icon drawable (nullable as some apps may not have icons)
 * @property isSystemApp Whether the app is a system app
 * @property isBlocked Whether the app is currently in the blocked list
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val isBlocked: Boolean = false
)
