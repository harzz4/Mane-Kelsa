package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.repository.WorkerRepository

class GiveThumbsUpUseCase(
    private val repository: WorkerRepository,
) {
    suspend operator fun invoke(workerId: String, raterId: String): Result<Unit> {
        return repository.giveThumbsUp(workerId, raterId)
    }
}
