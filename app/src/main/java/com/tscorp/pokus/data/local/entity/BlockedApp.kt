package com.tscorp.pokus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a blocked application stored in the Room database.
 *
 * @property packageName The unique package name of the blocked app (e.g., "com.instagram.android")
 * @property appName The display name of the app
 * @property isBlocked Whether the app is currently blocked
 * @property addedTimestamp The timestamp when the app was added to the block list
 */
@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis()
)
