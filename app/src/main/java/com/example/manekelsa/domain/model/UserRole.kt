package com.example.manekelsa.domain.model

enum class UserRole {
    WORKER,
    RESIDENT;

    companion object {
        fun fromStorageName(value: String?): UserRole? {
            return entries.firstOrNull { it.name == value }
        }
    }
}
