package com.example.manekelsa.data.repository

import android.util.Log
import com.example.manekelsa.data.firebase.FirebaseWorkerDataSource
import com.example.manekelsa.data.local.LocalPreferencesDataSource
import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.domain.repository.WorkerRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class WorkerRepositoryImpl @Inject constructor(
    private val firebaseWorkerDataSource: FirebaseWorkerDataSource,
    private val localPreferencesDataSource: LocalPreferencesDataSource,
) : WorkerRepository {
    override fun observeWorkers(): Flow<Result<List<WorkerProfile>>> = channelFlow {
        val localJob = launch {
            localPreferencesDataSource.workerProfiles.collectLatest { localWorkers ->
                send(Result.success(localWorkers.sortedByDescending { it.updatedAt }))
            }
        }

        val remoteJob = if (firebaseWorkerDataSource.isAvailable) {
            launch {
                firebaseWorkerDataSource.observeWorkers().collectLatest { result ->
                    result.onSuccess { remoteWorkers ->
                        val localWorkers = localPreferencesDataSource.workerProfiles.first()
                        val mergedWorkers = mergeWorkers(
                            localWorkers = localWorkers,
                            remoteWorkers = remoteWorkers,
                        )
                        localPreferencesDataSource.saveWorkerProfiles(mergedWorkers)
                        send(Result.success(mergedWorkers))
                    }
                    // Remote failures are intentionally ignored here because local cache is
                    // the source of truth for the UI. This keeps the app usable even when
                    // Firebase is unavailable, slow, or not configured correctly.
                }
            }
        } else {
            null
        }

        awaitClose {
            localJob.cancel()
            remoteJob?.cancel()
        }
    }

    override fun observeWorkerProfile(workerId: String): Flow<Result<WorkerProfile?>> = channelFlow {
        val localJob = launch {
            localPreferencesDataSource.workerProfiles
                .map { profiles -> profiles.firstOrNull { it.id == workerId } }
                .collectLatest { localProfile ->
                    send(Result.success(localProfile))
                }
        }

        val remoteJob = if (firebaseWorkerDataSource.isAvailable) {
            launch {
                firebaseWorkerDataSource.observeWorkerProfile(workerId).collectLatest { result ->
                    val localProfile = localPreferencesDataSource.workerProfiles.first()
                        .firstOrNull { it.id == workerId }

                    result.onSuccess { remoteProfile ->
                        val profileToUse = chooseNewestProfile(
                            localProfile = localProfile,
                            remoteProfile = remoteProfile,
                        )

                        if (profileToUse != null) {
                            localPreferencesDataSource.saveWorkerProfile(profileToUse)
                        }
                        send(Result.success(profileToUse))
                    }
                    // Remote failures are intentionally ignored because the local flow above
                    // already keeps the screen populated from saved device data.
                }
            }
        } else {
            null
        }

        awaitClose {
            localJob.cancel()
            remoteJob?.cancel()
        }
    }

    override suspend fun saveWorkerProfile(profile: WorkerProfile): Result<Unit> {
        return runCatching {
            localPreferencesDataSource.saveWorkerProfile(profile)

            runRemoteBestEffort {
                firebaseWorkerDataSource.saveWorkerProfile(profile)
                    .onSuccess { remoteSavedProfile ->
                        localPreferencesDataSource.saveWorkerProfile(
                            remoteSavedProfile.withLocalOnlyPhoto(profile),
                        )
                        Log.d("Sourik","Success : $remoteSavedProfile")
                    }.onFailure {
                        Log.d("Sourik", "Error : $it")
                    }
            }
        }.map { Unit }
    }

    override suspend fun updateAvailability(
        workerId: String,
        isAvailableToday: Boolean,
    ): Result<Unit> {
        return runCatching {
            localPreferencesDataSource.updateWorkerAvailability(workerId, isAvailableToday)

            runRemoteBestEffort {
                firebaseWorkerDataSource.updateAvailability(workerId, isAvailableToday)
            }
        }.map { Unit }
    }

    override suspend fun giveThumbsUp(workerId: String, raterId: String): Result<Unit> {
        return runCatching {
            localPreferencesDataSource.giveThumbsUp(workerId, raterId)

            runRemoteBestEffort {
                firebaseWorkerDataSource.giveThumbsUp(workerId, raterId)
            }
        }.map { Unit }
    }

    private suspend fun runRemoteBestEffort(block: suspend () -> Unit) {
        if (!firebaseWorkerDataSource.isAvailable) return

        withTimeoutOrNull(REMOTE_SYNC_TIMEOUT_MS) {
            runCatching { block() }
        }
    }

    private fun mergeWorkers(
        localWorkers: List<WorkerProfile>,
        remoteWorkers: List<WorkerProfile>,
    ): List<WorkerProfile> {
        val localById = localWorkers.associateBy { it.id }
        val remoteById = remoteWorkers.associateBy { it.id }

        return (localById.keys + remoteById.keys)
            .mapNotNull { workerId ->
                val local = localById[workerId]
                val remote = remoteById[workerId]

                chooseNewestProfile(
                    localProfile = local,
                    remoteProfile = remote,
                )
            }
            .sortedByDescending { it.updatedAt }
    }

    private fun chooseNewestProfile(
        localProfile: WorkerProfile?,
        remoteProfile: WorkerProfile?,
    ): WorkerProfile? {
        return when {
            localProfile == null -> remoteProfile
            remoteProfile == null -> localProfile
            localProfile.updatedAt > remoteProfile.updatedAt -> localProfile
            else -> remoteProfile.withLocalOnlyPhoto(localProfile)
        }
    }

    private fun WorkerProfile.withLocalOnlyPhoto(localProfile: WorkerProfile?): WorkerProfile {
        val localPhotoUrl = localProfile?.photoUrl
        return if (photoUrl.isNullOrBlank() && !localPhotoUrl.isNullOrBlank()) {
            copy(photoUrl = localPhotoUrl)
        } else {
            this
        }
    }

    private companion object {
        const val REMOTE_SYNC_TIMEOUT_MS = 2_500L
    }
}
