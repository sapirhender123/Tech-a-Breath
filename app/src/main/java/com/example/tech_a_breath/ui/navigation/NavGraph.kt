package com.example.tech_a_breath.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tech_a_breath.MainAppScreen
import com.example.tech_a_breath.data.repository.DashboardRepository
import com.example.tech_a_breath.ui.dashboard.DashboardRoot
import com.example.tech_a_breath.ui.dashboard.friendly.AdjustmentSuggestionScreen
import com.example.tech_a_breath.ui.dashboard.friendly.FriendlyDashboardScreen
import com.example.tech_a_breath.ui.dashboard.friendly.FriendlyDashboardViewModel
import com.example.tech_a_breath.data.prefs.WeeklyRatingStore

/**
 * Route names for every destination in the app.
 */
object Routes {
    const val WELCOME = "welcome"
    const val MAIN = "main"
    const val DASHBOARD = "dashboard"
    const val FRIENDLY_CHECKIN = "friendly_checkin"
    const val FRIENDLY_ADJUSTMENT = "friendly_adjustment/{rating}"
    fun friendlyAdjustment(rating: Int) = "friendly_adjustment/$rating"
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
                onOpenDashboard = { navController.navigate(Routes.DASHBOARD) },
                onOpenFriendlyDashboard = { navController.navigate(Routes.FRIENDLY_CHECKIN) }
            )
        }

        composable(Routes.DASHBOARD) {
            val repository = remember { databaseProvider() }
            DashboardRoot(repository = repository)
        }

        composable(Routes.FRIENDLY_CHECKIN) {
            val repository = remember { databaseProvider() }
            val ratingStore = remember {
                WeeklyRatingStore(navController.context)
            }
            val vm: FriendlyDashboardViewModel = viewModel(
                factory = FriendlyDashboardViewModel.Factory(repository, ratingStore)
            )
            FriendlyDashboardScreen(
                viewModel = vm,
                onContinue = { rating ->
                    navController.navigate(Routes.friendlyAdjustment(rating))
                }
            )
        }

        composable(
            route = Routes.FRIENDLY_ADJUSTMENT,
            arguments = listOf(navArgument("rating") { type = NavType.IntType })
        ) { backStackEntry ->
            val rating = backStackEntry.arguments?.getInt("rating") ?: 3
            val repository = remember { databaseProvider() }
            val ratingStore = remember {
                WeeklyRatingStore(navController.context)
            }
            val vm: FriendlyDashboardViewModel = viewModel(
                factory = FriendlyDashboardViewModel.Factory(repository, ratingStore)
            )
            AdjustmentSuggestionScreen(
                rating = rating,
                onBack = { navController.popBackStack() },
                onApply = {
                    vm.applyMaskingReduction()
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onKeep = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}
