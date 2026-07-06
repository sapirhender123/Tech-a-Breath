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
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
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
                            onStop = { TriggerManager.stopIntervention() }
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
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startMonitoringService()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
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
