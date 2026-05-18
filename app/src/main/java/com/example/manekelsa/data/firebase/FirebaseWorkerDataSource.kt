package com.example.manekelsa.data.firebase

import android.net.Uri
import com.example.manekelsa.domain.model.ServiceType
import com.example.manekelsa.domain.model.WorkerProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseWorkerDataSource(
    private val firestore: FirebaseFirestore?,
    private val storage: FirebaseStorage?,
) {
    val isAvailable: Boolean
        get() = firestore != null

    fun observeWorkers(): Flow<Result<List<WorkerProfile>>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(Result.failure(FirebaseUnavailableException()))
            close()
            return@callbackFlow
        }

        val registration = db.collection(WORKERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val workers = snapshot
                    ?.documents
                    ?.mapNotNull { document -> document.toWorkerProfileOrNull() }
                    .orEmpty()

                trySend(Result.success(workers))
            }

        awaitClose { registration.remove() }
    }

    fun observeWorkerProfile(workerId: String): Flow<Result<WorkerProfile?>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(Result.failure(FirebaseUnavailableException()))
            close()
            return@callbackFlow
        }

        val registration = db.collection(WORKERS_COLLECTION)
            .document(workerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                trySend(Result.success(snapshot?.toWorkerProfileOrNull()))
            }

        awaitClose { registration.remove() }
    }

    suspend fun saveWorkerProfile(profile: WorkerProfile): Result<WorkerProfile> {
        val db = firestore ?: return Result.failure(FirebaseUnavailableException())
        return runCatching {
            val profileToSave = uploadPhotoIfNeeded(profile)
            db.collection(WORKERS_COLLECTION)
                .document(profileToSave.id)
                .set(profileToSave.toFirestoreMap(), SetOptions.merge())
                .await()
            profileToSave
        }
    }

    suspend fun updateAvailability(workerId: String, isAvailableToday: Boolean): Result<Unit> {
        val db = firestore ?: return Result.failure(FirebaseUnavailableException())
        return runCatching {
            db.collection(WORKERS_COLLECTION)
                .document(workerId)
                .set(
                    mapOf(
                        "isAvailableToday" to isAvailableToday,
                        "updatedAt" to System.currentTimeMillis(),
                    ),
                    SetOptions.merge(),
                )
                .await()
            Unit
        }
    }

    suspend fun giveThumbsUp(workerId: String, raterId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(FirebaseUnavailableException())
        return runCatching {
            require(raterId.isNotBlank()) { "raterId cannot be blank" }

            val workerRef = db.collection(WORKERS_COLLECTION).document(workerId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(workerRef)
                val existingRatedBy = snapshot.readRatedBy()

                if (!existingRatedBy.contains(raterId)) {
                    val updatedRatedBy = existingRatedBy + raterId
                    transaction.set(
                        workerRef,
                        mapOf(
                            "ratedBy" to updatedRatedBy,
                            "thumbsUpCount" to updatedRatedBy.size,
                            "updatedAt" to System.currentTimeMillis(),
                        ),
                        SetOptions.merge(),
                    )
                }
            }.await()
            Unit
        }
    }

    private suspend fun uploadPhotoIfNeeded(profile: WorkerProfile): WorkerProfile {
        val photoUrl = profile.photoUrl?.trim()
        if (photoUrl.isNullOrBlank() || photoUrl.isRemoteUrl()) return profile

        val firebaseStorage = storage ?: return profile.copy(photoUrl = null)
        val photoUri = Uri.parse(photoUrl)
        val photoRef = firebaseStorage.reference
            .child("worker_profile_photos")
            .child(profile.id)
            .child("${System.currentTimeMillis()}.jpg")

        return runCatching {
            photoRef.putFile(photoUri).await()
            val downloadUrl = photoRef.downloadUrl.await().toString()
            profile.copy(photoUrl = downloadUrl)
        }.getOrElse {
            profile.copy(photoUrl = null)
        }
    }

    private fun String.isRemoteUrl(): Boolean {
        return startsWith("https://", ignoreCase = true) ||
            startsWith("http://", ignoreCase = true) ||
            startsWith("gs://", ignoreCase = true)
    }

    private fun WorkerProfile.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "phoneNumber" to phoneNumber,
            "photoUrl" to photoUrl,
            "serviceType" to serviceType.name,
            "area" to area,
            "street" to street,
            "dailyRate" to dailyRate,
            "twoHourRate" to twoHourRate,
            "experienceYears" to experienceYears,
            "description" to description,
            "isAvailableToday" to isAvailableToday,
            "thumbsUpCount" to thumbsUpCount,
            "ratedBy" to ratedBy,
            "updatedAt" to updatedAt,
        )
    }

    private fun DocumentSnapshot.toWorkerProfileOrNull(): WorkerProfile? {
        if (!exists()) return null

        val ratedBy = readRatedBy()
        return WorkerProfile(
            id = getString("id").orEmpty().ifBlank { id },
            name = getString("name").orEmpty(),
            phoneNumber = getString("phoneNumber").orEmpty(),
            photoUrl = getString("photoUrl")?.takeIf { it.isNotBlank() },
            serviceType = ServiceType.fromStorageName(getString("serviceType")),
            area = getString("area").orEmpty(),
            street = getString("street")?.takeIf { it.isNotBlank() },
            dailyRate = getLong("dailyRate")?.toInt() ?: 0,
            twoHourRate = getLong("twoHourRate")?.toInt() ?: 0,
            experienceYears = getLong("experienceYears")?.toInt() ?: 0,
            description = getString("description").orEmpty(),
            isAvailableToday = getBoolean("isAvailableToday") ?: false,
            thumbsUpCount = getLong("thumbsUpCount")?.toInt() ?: ratedBy.size,
            ratedBy = ratedBy,
            updatedAt = getLong("updatedAt") ?: 0L,
        )
    }

    private fun DocumentSnapshot.readRatedBy(): List<String> {
        val ratedByValue = get("ratedBy")
        return when (ratedByValue) {
            is List<*> -> ratedByValue.mapNotNull { it as? String }
            is Map<*, *> -> ratedByValue
                .filterValues { value -> value == true }
                .keys
                .mapNotNull { key -> key as? String }
            else -> emptyList()
        }.distinct()
    }

    private companion object {
        const val WORKERS_COLLECTION = "workers"
    }
}
