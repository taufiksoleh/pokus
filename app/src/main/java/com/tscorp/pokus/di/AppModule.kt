package com.tscorp.pokus.di

import android.content.Context
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
     * Provides the application context.
     * Use this when you need a context that outlives activities.
     */
    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context

    // TODO: Add more providers in later phases:
    // - PreferencesManager (Phase 2)
    // - PokusDatabase (Phase 2)
    // - AppRepository (Phase 2)
}
