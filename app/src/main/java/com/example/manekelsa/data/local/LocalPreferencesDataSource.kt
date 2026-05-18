package com.example.manekelsa.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.domain.model.ServiceType
import com.example.manekelsa.domain.model.UserRole
import com.example.manekelsa.domain.model.WorkerProfile
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class LocalPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val userRole: Flow<UserRole?> = dataStore.data
        .safePreferences()
        .map { preferences -> UserRole.fromStorageName(preferences[USER_ROLE_KEY]) }

    val workerId: Flow<String?> = dataStore.data
        .safePreferences()
        .map { preferences -> preferences[WORKER_ID_KEY] }

    val appLanguage: Flow<AppLanguage> = dataStore.data
        .safePreferences()
        .map { preferences -> AppLanguage.fromStorageCode(preferences[APP_LANGUAGE_KEY]) }

    val workerProfiles: Flow<List<WorkerProfile>> = dataStore.data
        .safePreferences()
        .map { preferences -> decodeWorkerProfiles(preferences[WORKER_PROFILES_KEY]) }

    suspend fun saveUserRole(role: UserRole) {
        dataStore.edit { preferences ->
            preferences[USER_ROLE_KEY] = role.name
        }
    }

    suspend fun clearUserRole() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ROLE_KEY)
        }
    }

    suspend fun saveAppLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[APP_LANGUAGE_KEY] = language.storageCode
        }
    }

    suspend fun saveWorkerProfile(profile: WorkerProfile) {
        dataStore.edit { preferences ->
            val profiles = decodeWorkerProfiles(preferences[WORKER_PROFILES_KEY])
            val updatedProfiles = profiles
                .filterNot { it.id == profile.id }
                .plus(profile)
            preferences[WORKER_PROFILES_KEY] = encodeWorkerProfiles(updatedProfiles)
        }
    }

    suspend fun saveWorkerProfiles(profiles: List<WorkerProfile>) {
        dataStore.edit { preferences ->
            preferences[WORKER_PROFILES_KEY] = encodeWorkerProfiles(profiles)
        }
    }

    suspend fun updateWorkerAvailability(workerId: String, isAvailableToday: Boolean) {
        dataStore.edit { preferences ->
            val profiles = decodeWorkerProfiles(preferences[WORKER_PROFILES_KEY])
            val updatedProfiles = profiles.map { profile ->
                if (profile.id == workerId) {
                    profile.copy(
                        isAvailableToday = isAvailableToday,
                        updatedAt = System.currentTimeMillis(),
                    )
                } else {
                    profile
                }
            }
            preferences[WORKER_PROFILES_KEY] = encodeWorkerProfiles(updatedProfiles)
        }
    }

    suspend fun giveThumbsUp(workerId: String, raterId: String) {
        dataStore.edit { preferences ->
            val profiles = decodeWorkerProfiles(preferences[WORKER_PROFILES_KEY])
            val updatedProfiles = profiles.map { profile ->
                if (profile.id == workerId && !profile.ratedBy.contains(raterId)) {
                    val updatedRatedBy = profile.ratedBy + raterId
                    profile.copy(
                        thumbsUpCount = maxOf(profile.thumbsUpCount + 1, updatedRatedBy.size),
                        ratedBy = updatedRatedBy,
                        updatedAt = System.currentTimeMillis(),
                    )
                } else {
                    profile
                }
            }
            preferences[WORKER_PROFILES_KEY] = encodeWorkerProfiles(updatedProfiles)
        }
    }

    suspend fun getOrCreateWorkerId(): String {
        return getOrCreateId(WORKER_ID_KEY, "worker")
    }

    suspend fun getOrCreateDeviceId(): String {
        return getOrCreateId(DEVICE_ID_KEY, "device")
    }

    private suspend fun getOrCreateId(key: Preferences.Key<String>, prefix: String): String {
        val existing = dataStore.data.safePreferences().first()[key]
        if (!existing.isNullOrBlank()) return existing

        val newId = "$prefix-${UUID.randomUUID()}"
        dataStore.edit { preferences ->
            preferences[key] = newId
        }
        return newId
    }

    private fun encodeWorkerProfiles(profiles: List<WorkerProfile>): String {
        val array = JSONArray()
        profiles.forEach { profile ->
            array.put(profile.toJson())
        }
        return array.toString()
    }

    private fun decodeWorkerProfiles(value: String?): List<WorkerProfile> {
        if (value.isNullOrBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(value)
            buildList {
                repeat(array.length()) { index ->
                    array.optJSONObject(index)?.toWorkerProfileOrNull()?.let(::add)
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun WorkerProfile.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("phoneNumber", phoneNumber)
            put("photoUrl", photoUrl)
            put("serviceType", serviceType.name)
            put("area", area)
            put("street", street)
            put("dailyRate", dailyRate)
            put("twoHourRate", twoHourRate)
            put("experienceYears", experienceYears)
            put("description", description)
            put("isAvailableToday", isAvailableToday)
            put("thumbsUpCount", thumbsUpCount)
            put("ratedBy", JSONArray(ratedBy))
            put("updatedAt", updatedAt)
        }
    }

    private fun JSONObject.toWorkerProfileOrNull(): WorkerProfile? {
        val id = optString("id").takeIf { it.isNotBlank() } ?: return null
        return WorkerProfile(
            id = id,
            name = optString("name"),
            phoneNumber = optString("phoneNumber"),
            photoUrl = optionalString("photoUrl"),
            serviceType = ServiceType.fromStorageName(optString("serviceType")),
            area = optString("area"),
            street = optionalString("street"),
            dailyRate = optInt("dailyRate", 0),
            twoHourRate = optInt("twoHourRate", 0),
            experienceYears = optInt("experienceYears", 0),
            description = optString("description"),
            isAvailableToday = optBoolean("isAvailableToday", false),
            thumbsUpCount = optInt("thumbsUpCount", 0),
            ratedBy = optJSONArray("ratedBy").orEmptyStrings(),
            updatedAt = optLong("updatedAt", 0L),
        )
    }

    private fun JSONObject.optionalString(name: String): String? {
        return optString(name)
            .takeIf { it.isNotBlank() && it != "null" }
    }

    private fun JSONArray?.orEmptyStrings(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            repeat(length()) { index ->
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    private fun Flow<Preferences>.safePreferences(): Flow<Preferences> {
        return catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
    }

    private companion object {
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        val WORKER_ID_KEY = stringPreferencesKey("worker_id")
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        val WORKER_PROFILES_KEY = stringPreferencesKey("worker_profiles")
    }
}
