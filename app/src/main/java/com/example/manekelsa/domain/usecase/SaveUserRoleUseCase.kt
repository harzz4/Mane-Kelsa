package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.model.UserRole
import com.example.manekelsa.domain.repository.UserPreferencesRepository

class SaveUserRoleUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(role: UserRole) {
        repository.saveUserRole(role)
    }
}
