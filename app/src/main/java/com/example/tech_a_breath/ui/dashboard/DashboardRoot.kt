package com.example.tech_a_breath.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tech_a_breath.data.repository.DashboardRepository
import com.example.tech_a_breath.ui.dashboard.effectiveness.MaskingEffectivenessScreen
import com.example.tech_a_breath.ui.dashboard.effectiveness.MaskingEffectivenessViewModel
import com.example.tech_a_breath.ui.dashboard.monthly.MonthlySummaryScreen
import com.example.tech_a_breath.ui.dashboard.monthly.MonthlySummaryViewModel
import com.example.tech_a_breath.ui.dashboard.weekly.WeeklySummaryScreen
import com.example.tech_a_breath.ui.dashboard.weekly.WeeklySummaryViewModel

// ── Navigation model ──────────────────────────────────────────────────────────

private sealed class DashboardTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Weekly : DashboardTab("weekly", "Weekly", Icons.Outlined.DateRange)
    object Monthly : DashboardTab("monthly", "Monthly", Icons.Outlined.CalendarMonth)
    object Effectiveness : DashboardTab("effectiveness", "Masking", Icons.Outlined.BarChart)
}

private val tabs = listOf(
    DashboardTab.Weekly,
    DashboardTab.Monthly,
    DashboardTab.Effectiveness
)

// ── Root composable ───────────────────────────────────────────────────────────

/**
 * Entry-point for the dashboard feature.
 * Call this from MainActivity (or any parent composable) and pass the shared repository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoot(repository: DashboardRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination?.route) {
                            DashboardTab.Weekly.route       -> "Weekly Summary"
                            DashboardTab.Monthly.route      -> "Monthly Summary"
                            DashboardTab.Effectiveness.route -> "Masking Effectiveness"
                            else                             -> "Dashboard"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEach { tab ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == tab.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon  = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = DashboardTab.Weekly.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(DashboardTab.Weekly.route) {
                val vm: WeeklySummaryViewModel = viewModel(
                    factory = WeeklySummaryViewModel.Factory(repository)
                )
                WeeklySummaryScreen(vm)
            }
            composable(DashboardTab.Monthly.route) {
                val vm: MonthlySummaryViewModel = viewModel(
                    factory = MonthlySummaryViewModel.Factory(repository)
                )
                MonthlySummaryScreen(vm)
            }
            composable(DashboardTab.Effectiveness.route) {
                val vm: MaskingEffectivenessViewModel = viewModel(
                    factory = MaskingEffectivenessViewModel.Factory(repository)
                )
                MaskingEffectivenessScreen(vm)
            }
        }
    }
}
