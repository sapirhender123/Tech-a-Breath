package com.example.tech_a_breath.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tech_a_breath.MainAppScreen
import com.example.tech_a_breath.data.repository.DashboardRepository
import com.example.tech_a_breath.ui.dashboard.DashboardRoot
import com.example.tech_a_breath.ui.screens.LoginScreen
import com.example.tech_a_breath.ui.screens.WelcomeScreen

/**
 * Route names for every destination in the app.
 */
object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val DASHBOARD = "dashboard"
}

@Composable
fun TechABreathNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.WELCOME,
    onStartProtection: () -> Unit,
    onStopProtection: () -> Unit,
    databaseProvider: () -> DashboardRepository,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onGetStartedClick = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSignInSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainAppScreen(
                onStartProtection = onStartProtection,
                onStopProtection = onStopProtection,
                onOpenDashboard = { navController.navigate(Routes.DASHBOARD) }
            )
        }

        composable(Routes.DASHBOARD) {
            val repository = remember { databaseProvider() }
            DashboardRoot(repository = repository)
        }
    }
}
