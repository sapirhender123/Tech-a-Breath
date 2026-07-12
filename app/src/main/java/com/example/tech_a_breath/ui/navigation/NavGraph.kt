package com.example.tech_a_breath.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tech_a_breath.TriggerManager
import com.example.tech_a_breath.ui.InterventionScreen
import com.example.tech_a_breath.ui.ListeningScreen
import com.example.tech_a_breath.ui.TriggerProtectionSettingsScreen
import com.example.tech_a_breath.ui.screens.LoginScreen
import com.example.tech_a_breath.ui.screens.WelcomeScreen

/**
 * Route names for every destination in the app.
 */
object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val MAIN = "main"
}

@Composable
fun TechABreathNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.WELCOME,
    onStartProtection: () -> Unit,
    onStopProtection: () -> Unit,
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
            )
        }
    }
}

/**
 * Hosts the original settings / monitoring / intervention flow that previously
 * lived inline in MainActivity. Extracted here so it can be dropped into the
 * nav graph as a single destination.
 */
@Composable
private fun MainAppScreen(
    onStartProtection: () -> Unit,
    onStopProtection: () -> Unit,
) {
    var currentScreen by remember { mutableStateOf("settings") }
    val activeIntervention by TriggerManager.activeIntervention.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (activeIntervention != null) {
            InterventionScreen(
                mode = activeIntervention!!,
                onStop = { TriggerManager.stopIntervention(force = true) }
            )
        } else if (currentScreen == "settings") {
            TriggerProtectionSettingsScreen(onStartProtection = {
                currentScreen = "monitoring"
                onStartProtection()
            })
        } else {
            ListeningScreen(onOpenSettings = {
                currentScreen = "settings"
            })
        }
    }
}
