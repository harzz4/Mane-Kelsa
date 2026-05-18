package com.example.manekelsa.domain.model

data class WorkerProfile(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUrl: String?,
    val serviceType: ServiceType,
    val area: String,
    val street: String?,
    val dailyRate: Int,
    val twoHourRate: Int,
    val experienceYears: Int,
    val description: String,
    val isAvailableToday: Boolean,
    val thumbsUpCount: Int,
    val ratedBy: List<String>,
    val updatedAt: Long,
) {
    val availabilityStatus: AvailabilityStatus
        get() = if (isAvailableToday) {
            AvailabilityStatus.AVAILABLE_TODAY
        } else {
            AvailabilityStatus.NOT_AVAILABLE
        }
}
