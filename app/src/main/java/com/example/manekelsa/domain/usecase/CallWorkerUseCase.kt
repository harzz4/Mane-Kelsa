package com.example.manekelsa.domain.usecase

class CallWorkerUseCase {
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val normalized = normalize(phoneNumber)
        return Regex("^(\\+91)?[6-9]\\d{9}$").matches(normalized)
    }

    operator fun invoke(phoneNumber: String): Result<String> {
        val normalized = normalize(phoneNumber)
        return if (isValidPhoneNumber(normalized)) {
            Result.success(normalized)
        } else {
            Result.failure(IllegalArgumentException("Invalid phone number"))
        }
    }

    private fun normalize(phoneNumber: String): String {
        return phoneNumber
            .trim()
            .replace(" ", "")
            .replace("-", "")
    }
}
