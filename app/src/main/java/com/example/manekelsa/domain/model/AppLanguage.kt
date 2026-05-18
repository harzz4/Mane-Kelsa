package com.example.manekelsa.domain.model

enum class AppLanguage(val storageCode: String, val localeTag: String) {
    KANNADA(storageCode = "kn", localeTag = "kn"),
    ENGLISH(storageCode = "en", localeTag = "en"),
    ;

    companion object {
        val DEFAULT = KANNADA

        fun fromStorageCode(value: String?): AppLanguage {
            return entries.firstOrNull { it.storageCode == value } ?: DEFAULT
        }
    }
}
