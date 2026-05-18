package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.repository.UserPreferencesRepository

class GetWorkerIdUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(): String = repository.getOrCreateWorkerId()
}
