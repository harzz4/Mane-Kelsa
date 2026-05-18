package com.example.manekelsa.domain.repository

import com.example.manekelsa.domain.model.WorkerProfile
import kotlinx.coroutines.flow.Flow

interface WorkerRepository {
    fun observeWorkers(): Flow<Result<List<WorkerProfile>>>
    fun observeWorkerProfile(workerId: String): Flow<Result<WorkerProfile?>>
    suspend fun saveWorkerProfile(profile: WorkerProfile): Result<Unit>
    suspend fun updateAvailability(workerId: String, isAvailableToday: Boolean): Result<Unit>
    suspend fun giveThumbsUp(workerId: String, raterId: String): Result<Unit>
}
