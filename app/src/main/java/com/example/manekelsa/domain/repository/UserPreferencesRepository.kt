package com.example.manekelsa.domain.repository

import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userRole: Flow<UserRole?>
    val workerId: Flow<String?>
    val appLanguage: Flow<AppLanguage>

    suspend fun saveUserRole(role: UserRole)
    suspend fun clearUserRole()
    suspend fun getOrCreateWorkerId(): String
    suspend fun getOrCreateDeviceId(): String
    suspend fun saveAppLanguage(language: AppLanguage)
}
