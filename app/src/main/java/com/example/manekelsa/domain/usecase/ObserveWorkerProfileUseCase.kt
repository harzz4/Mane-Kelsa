package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.domain.repository.WorkerRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorkerProfileUseCase(
    private val repository: WorkerRepository,
) {
    operator fun invoke(workerId: String): Flow<Result<WorkerProfile?>> {
        return repository.observeWorkerProfile(workerId)
    }
}
