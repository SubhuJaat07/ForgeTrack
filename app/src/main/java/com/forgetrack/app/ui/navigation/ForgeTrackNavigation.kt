package com.forgetrack.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.forgetrack.app.ui.screens.analytics.AnalyticsScreen
import com.forgetrack.app.ui.screens.clients.ClientsScreen
import com.forgetrack.app.ui.screens.dashboard.DashboardScreen
import com.forgetrack.app.ui.screens.history.HistoryScreen
import com.forgetrack.app.ui.screens.job.CreateJobScreen
import com.forgetrack.app.ui.screens.job.JobDetailScreen
import com.forgetrack.app.ui.screens.onboarding.OnboardingScreen
import com.forgetrack.app.ui.screens.settings.SettingsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val JOBS = "jobs"
    const val JOB_DETAIL = "job/{jobId}"
    const val CREATE_JOB = "create_job"
    const val CLIENTS = "clients"
    const val HISTORY = "history"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
}

@Composable
fun ForgeTrackNavigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToJob = { jobId ->
                    navController.navigate("job/$jobId")
                },
                onCreateJob = {
                    navController.navigate(Routes.CREATE_JOB)
                }
            )
        }

        composable(Routes.JOBS) {
            val vm: com.forgetrack.app.ui.screens.job.JobsViewModel = hiltViewModel()
            com.forgetrack.app.ui.screens.job.JobsScreen(
                onJobClick = { jobId -> navController.navigate("job/$jobId") },
                onCreateJob = { navController.navigate(Routes.CREATE_JOB) },
                viewModel = vm
            )
        }

        composable(
            route = Routes.JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            JobDetailScreen(
                jobId = jobId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CREATE_JOB) {
            CreateJobScreen(
                onJobCreated = { jobId ->
                    navController.navigate("job/$jobId") {
                        popUpTo(Routes.CREATE_JOB) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Routes.CLIENTS) {
            ClientsScreen()
        }

        composable(Routes.HISTORY) {
            HistoryScreen()
        }

        composable(Routes.ANALYTICS) {
            AnalyticsScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}
