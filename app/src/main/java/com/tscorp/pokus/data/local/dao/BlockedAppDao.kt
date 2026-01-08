package com.tscorp.pokus.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tscorp.pokus.data.local.entity.BlockedApp
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for BlockedApp entity.
 * Provides CRUD operations for managing blocked applications.
 */
@Dao
interface BlockedAppDao {

    /**
     * Insert a new blocked app. If the app already exists, replace it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(blockedApp: BlockedApp)

    /**
     * Insert multiple blocked apps at once.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApps(blockedApps: List<BlockedApp>)

    /**
     * Update an existing blocked app.
     */
    @Update
    suspend fun updateBlockedApp(blockedApp: BlockedApp)

    /**
     * Delete a blocked app.
     */
    @Delete
    suspend fun deleteBlockedApp(blockedApp: BlockedApp)

    /**
     * Delete a blocked app by its package name.
     */
    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    /**
     * Get all blocked apps as a Flow for reactive updates.
     */
    @Query("SELECT * FROM blocked_apps ORDER BY addedTimestamp DESC")
    fun getAllBlockedApps(): Flow<List<BlockedApp>>

    /**
     * Get all currently blocked apps (where isBlocked = true).
     */
    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1 ORDER BY addedTimestamp DESC")
    fun getActiveBlockedApps(): Flow<List<BlockedApp>>

    /**
     * Get a specific blocked app by package name.
     */
    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getBlockedAppByPackage(packageName: String): BlockedApp?

    /**
     * Check if a package is blocked.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName AND isBlocked = 1)")
    suspend fun isAppBlocked(packageName: String): Boolean

    /**
     * Get count of blocked apps.
     */
    @Query("SELECT COUNT(*) FROM blocked_apps WHERE isBlocked = 1")
    fun getBlockedAppsCount(): Flow<Int>

    /**
     * Get all blocked package names as a list (non-Flow, for service use).
     */
    @Query("SELECT packageName FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getBlockedPackageNames(): List<String>

    /**
     * Update the blocked status of an app.
     */
    @Query("UPDATE blocked_apps SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun updateBlockedStatus(packageName: String, isBlocked: Boolean)

    /**
     * Delete all blocked apps.
     */
    @Query("DELETE FROM blocked_apps")
    suspend fun deleteAllBlockedApps()
}
