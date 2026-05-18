package com.example.manekelsa.di

import com.example.manekelsa.domain.repository.UserPreferencesRepository
import com.example.manekelsa.domain.repository.WorkerRepository
import com.example.manekelsa.domain.usecase.CallWorkerUseCase
import com.example.manekelsa.domain.usecase.ClearUserRoleUseCase
import com.example.manekelsa.domain.usecase.GenerateWorkerDescriptionUseCase
import com.example.manekelsa.domain.usecase.GetAppLanguageUseCase
import com.example.manekelsa.domain.usecase.GetDeviceIdUseCase
import com.example.manekelsa.domain.usecase.GetUserRoleUseCase
import com.example.manekelsa.domain.usecase.GetWorkerIdUseCase
import com.example.manekelsa.domain.usecase.GiveThumbsUpUseCase
import com.example.manekelsa.domain.usecase.ObserveWorkerProfileUseCase
import com.example.manekelsa.domain.usecase.ObserveWorkersUseCase
import com.example.manekelsa.domain.usecase.SaveAppLanguageUseCase
import com.example.manekelsa.domain.usecase.SaveUserRoleUseCase
import com.example.manekelsa.domain.usecase.SaveWorkerProfileUseCase
import com.example.manekelsa.domain.usecase.SortWorkersByAreaUseCase
import com.example.manekelsa.domain.usecase.UpdateAvailabilityUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideObserveWorkersUseCase(repository: WorkerRepository): ObserveWorkersUseCase {
        return ObserveWorkersUseCase(repository)
    }

    @Provides
    fun provideObserveWorkerProfileUseCase(repository: WorkerRepository): ObserveWorkerProfileUseCase {
        return ObserveWorkerProfileUseCase(repository)
    }

    @Provides
    fun provideSaveWorkerProfileUseCase(repository: WorkerRepository): SaveWorkerProfileUseCase {
        return SaveWorkerProfileUseCase(repository)
    }

    @Provides
    fun provideUpdateAvailabilityUseCase(repository: WorkerRepository): UpdateAvailabilityUseCase {
        return UpdateAvailabilityUseCase(repository)
    }

    @Provides
    fun provideGiveThumbsUpUseCase(repository: WorkerRepository): GiveThumbsUpUseCase {
        return GiveThumbsUpUseCase(repository)
    }

    @Provides
    fun provideSaveUserRoleUseCase(repository: UserPreferencesRepository): SaveUserRoleUseCase {
        return SaveUserRoleUseCase(repository)
    }

    @Provides
    fun provideGetUserRoleUseCase(repository: UserPreferencesRepository): GetUserRoleUseCase {
        return GetUserRoleUseCase(repository)
    }

    @Provides
    fun provideClearUserRoleUseCase(repository: UserPreferencesRepository): ClearUserRoleUseCase {
        return ClearUserRoleUseCase(repository)
    }

    @Provides
    fun provideGetWorkerIdUseCase(repository: UserPreferencesRepository): GetWorkerIdUseCase {
        return GetWorkerIdUseCase(repository)
    }

    @Provides
    fun provideGetDeviceIdUseCase(repository: UserPreferencesRepository): GetDeviceIdUseCase {
        return GetDeviceIdUseCase(repository)
    }

    @Provides
    fun provideGetAppLanguageUseCase(repository: UserPreferencesRepository): GetAppLanguageUseCase {
        return GetAppLanguageUseCase(repository)
    }

    @Provides
    fun provideSaveAppLanguageUseCase(repository: UserPreferencesRepository): SaveAppLanguageUseCase {
        return SaveAppLanguageUseCase(repository)
    }

    @Provides
    fun provideCallWorkerUseCase(): CallWorkerUseCase = CallWorkerUseCase()

    @Provides
    fun provideGenerateWorkerDescriptionUseCase(): GenerateWorkerDescriptionUseCase {
        return GenerateWorkerDescriptionUseCase()
    }

    @Provides
    fun provideSortWorkersByAreaUseCase(): SortWorkersByAreaUseCase {
        return SortWorkersByAreaUseCase()
    }
}
