package com.tscorp.pokus.di

import android.content.Context
import androidx.room.Room
import com.tscorp.pokus.data.local.PokusDatabase
import com.tscorp.pokus.data.local.dao.BlockedAppDao
import com.tscorp.pokus.data.preferences.PreferencesManager
import com.tscorp.pokus.data.repository.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies.
 * Dependencies provided here are available throughout the app lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun providePokusDatabase(
        @ApplicationContext context: Context
    ): PokusDatabase {
        return Room.databaseBuilder(
            context,
            PokusDatabase::class.java,
            PokusDatabase.DATABASE_NAME
        ).build()
    }

    /**
     * Provides the BlockedAppDao from the database.
     */
    @Provides
    @Singleton
    fun provideBlockedAppDao(database: PokusDatabase): BlockedAppDao {
        return database.blockedAppDao()
    }

    /**
     * Provides the PreferencesManager for DataStore preferences.
     */
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    /**
     * Provides the AppRepository for data access.
     */
    @Provides
    @Singleton
    fun provideAppRepository(
        blockedAppDao: BlockedAppDao,
        preferencesManager: PreferencesManager
    ): AppRepository {
        return AppRepository(blockedAppDao, preferencesManager)
    }
}
