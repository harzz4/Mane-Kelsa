package com.example.manekelsa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.manekelsa.presentation.screens.ResidentHomeScreen
import com.example.manekelsa.presentation.screens.RoleSelectionScreen
import com.example.manekelsa.presentation.screens.SettingsScreen
import com.example.manekelsa.presentation.screens.SplashScreen
import com.example.manekelsa.presentation.screens.WorkerDetailsScreen
import com.example.manekelsa.presentation.screens.WorkerHomeScreen
import com.example.manekelsa.presentation.screens.WorkerProfileEditScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToRoleSelection = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToWorkerHome = {
                    navController.navigate(Screen.WorkerHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToResidentHome = {
                    navController.navigate(Screen.ResidentHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onWorkerSelected = {
                    navController.navigate(Screen.WorkerHome.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                onResidentSelected = {
                    navController.navigate(Screen.ResidentHome.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.WorkerHome.route) {
            WorkerHomeScreen(
                onEditProfile = { navController.navigate(Screen.WorkerProfileEdit.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.WorkerProfileEdit.route) {
            WorkerProfileEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Screen.ResidentHome.route) {
            ResidentHomeScreen(
                onWorkerClick = { workerId ->
                    navController.navigate(Screen.WorkerDetails.createRoute(workerId))
                },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(
            route = Screen.WorkerDetails.route,
            arguments = listOf(navArgument(Screen.WorkerDetails.WORKER_ID_ARG) { type = NavType.StringType }),
        ) {
            WorkerDetailsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onRoleReset = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
