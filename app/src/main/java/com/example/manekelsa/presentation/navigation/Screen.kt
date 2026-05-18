package com.example.manekelsa.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object RoleSelection : Screen("role_selection")
    data object WorkerHome : Screen("worker_home")
    data object WorkerProfileEdit : Screen("worker_profile_edit")
    data object ResidentHome : Screen("resident_home")
    data object Settings : Screen("settings")
    data object WorkerDetails : Screen("worker_details/{workerId}") {
        const val WORKER_ID_ARG = "workerId"

        fun createRoute(workerId: String): String {
            return "worker_details/${Uri.encode(workerId)}"
        }
    }
}
