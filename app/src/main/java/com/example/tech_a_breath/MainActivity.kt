package com.example.tech_a_breath

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.tech_a_breath.service.MonitoringService
import com.example.tech_a_breath.ui.InterventionMode
import kotlinx.coroutines.launch
import com.example.tech_a_breath.ui.InterventionScreen
import com.example.tech_a_breath.ui.ListeningScreen
import com.example.tech_a_breath.ui.TriggerProtectionSettingsScreen
import com.example.tech_a_breath.ui.theme.TechABreathTheme

class MainActivity : ComponentActivity() {

    private var isServiceStarted = false

    // Launcher for the permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startMonitoringService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TechABreathTheme {
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
                            checkPermissionsAndStart()
                        })
                    } else {
                        ListeningScreen(onOpenSettings = {
                            currentScreen = "settings"
                        })
                    }
                }
            }
        }
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startMonitoringService()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startMonitoringService() {
        if (isServiceStarted) return
        val intent = Intent(this, MonitoringService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServiceStarted = true
    }
}
