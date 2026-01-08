package com.tscorp.pokus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tscorp.pokus.data.local.dao.BlockedAppDao
import com.tscorp.pokus.data.local.entity.BlockedApp

/**
 * Room database for Pokus application.
 * Stores blocked apps and related data.
 */
@Database(
    entities = [BlockedApp::class],
    version = 1,
    exportSchema = true
)
abstract class PokusDatabase : RoomDatabase() {

    /**
     * Provides access to BlockedAppDao for blocked app operations.
     */
    abstract fun blockedAppDao(): BlockedAppDao

    companion object {
        const val DATABASE_NAME = "pokus_database"
    }
}
