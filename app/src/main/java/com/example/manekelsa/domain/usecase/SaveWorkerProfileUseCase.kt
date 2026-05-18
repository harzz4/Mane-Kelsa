package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.domain.repository.WorkerRepository

class SaveWorkerProfileUseCase(
    private val repository: WorkerRepository,
) {
    suspend operator fun invoke(profile: WorkerProfile): Result<Unit> {
        return repository.saveWorkerProfile(profile)
    }
}
