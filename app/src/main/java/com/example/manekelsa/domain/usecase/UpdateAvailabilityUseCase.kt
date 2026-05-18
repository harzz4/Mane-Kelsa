package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.repository.WorkerRepository

class UpdateAvailabilityUseCase(
    private val repository: WorkerRepository,
) {
    suspend operator fun invoke(workerId: String, isAvailableToday: Boolean): Result<Unit> {
        return repository.updateAvailability(workerId, isAvailableToday)
    }
}
