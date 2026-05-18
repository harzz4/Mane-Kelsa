package com.example.manekelsa.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.manekelsa.data.local.LocalPreferencesDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return context.userPreferencesDataStore
    }

    @Provides
    @Singleton
    fun provideLocalPreferencesDataSource(
        dataStore: DataStore<Preferences>,
    ): LocalPreferencesDataSource {
        return LocalPreferencesDataSource(dataStore)
    }
}
