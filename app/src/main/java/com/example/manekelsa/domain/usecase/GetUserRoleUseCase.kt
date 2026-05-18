package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.model.UserRole
import com.example.manekelsa.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetUserRoleUseCase(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<UserRole?> = repository.userRole
}
