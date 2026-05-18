package com.example.manekelsa.di

import com.example.manekelsa.data.repository.UserPreferencesRepositoryImpl
import com.example.manekelsa.data.repository.WorkerRepositoryImpl
import com.example.manekelsa.domain.repository.UserPreferencesRepository
import com.example.manekelsa.domain.repository.WorkerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWorkerRepository(
        impl: WorkerRepositoryImpl,
    ): WorkerRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository
}
