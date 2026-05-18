package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.repository.UserPreferencesRepository

class ClearUserRoleUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke() {
        repository.clearUserRole()
    }
}
