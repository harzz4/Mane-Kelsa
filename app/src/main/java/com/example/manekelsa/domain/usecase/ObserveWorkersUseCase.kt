package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.domain.repository.WorkerRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorkersUseCase(
    private val repository: WorkerRepository,
) {
    operator fun invoke(): Flow<Result<List<WorkerProfile>>> {
        return repository.observeWorkers()
    }
}
