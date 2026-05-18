package com.example.manekelsa.domain.model

enum class ServiceType {
    HOUSE_CLEANING,
    COOKING,
    CHILD_CARE,
    ELDER_CARE,
    GARDENING,
    DRIVER,
    LAUNDRY,
    OTHER;

    companion object {
        fun fromStorageName(value: String?): ServiceType {
            return entries.firstOrNull { it.name == value } ?: HOUSE_CLEANING
        }
    }
}
