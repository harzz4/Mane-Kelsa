package com.example.manekelsa.data.repository

import com.example.manekelsa.data.local.LocalPreferencesDataSource
import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.domain.model.UserRole
import com.example.manekelsa.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val localPreferencesDataSource: LocalPreferencesDataSource,
) : UserPreferencesRepository {
    override val userRole: Flow<UserRole?> = localPreferencesDataSource.userRole
    override val workerId: Flow<String?> = localPreferencesDataSource.workerId
    override val appLanguage: Flow<AppLanguage> = localPreferencesDataSource.appLanguage

    override suspend fun saveUserRole(role: UserRole) {
        localPreferencesDataSource.saveUserRole(role)
        when (role) {
            UserRole.WORKER -> localPreferencesDataSource.getOrCreateWorkerId()
            UserRole.RESIDENT -> localPreferencesDataSource.getOrCreateDeviceId()
        }
    }

    override suspend fun clearUserRole() {
        localPreferencesDataSource.clearUserRole()
    }

    override suspend fun getOrCreateWorkerId(): String {
        return localPreferencesDataSource.getOrCreateWorkerId()
    }

    override suspend fun getOrCreateDeviceId(): String {
        return localPreferencesDataSource.getOrCreateDeviceId()
    }

    override suspend fun saveAppLanguage(language: AppLanguage) {
        localPreferencesDataSource.saveAppLanguage(language)
    }
}
