package com.example.manekelsa.presentation.common

import androidx.annotation.StringRes
import com.example.manekelsa.R
import com.example.manekelsa.domain.model.AvailabilityStatus
import com.example.manekelsa.domain.model.ServiceType
import com.example.manekelsa.domain.model.UserRole

@StringRes
fun AvailabilityStatus.labelRes(): Int {
    return when (this) {
        AvailabilityStatus.AVAILABLE_TODAY -> R.string.available_today
        AvailabilityStatus.NOT_AVAILABLE -> R.string.not_available
    }
}

@StringRes
fun ServiceType.labelRes(): Int {
    return when (this) {
        ServiceType.HOUSE_CLEANING -> R.string.service_house_cleaning
        ServiceType.COOKING -> R.string.service_cooking
        ServiceType.CHILD_CARE -> R.string.service_child_care
        ServiceType.ELDER_CARE -> R.string.service_elder_care
        ServiceType.GARDENING -> R.string.service_gardening
        ServiceType.DRIVER -> R.string.service_driver
        ServiceType.LAUNDRY -> R.string.service_laundry
        ServiceType.OTHER -> R.string.service_other
    }
}

@StringRes
fun UserRole.labelRes(): Int {
    return when (this) {
        UserRole.WORKER -> R.string.worker_role
        UserRole.RESIDENT -> R.string.resident_role
    }
}
