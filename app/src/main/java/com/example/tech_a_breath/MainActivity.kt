package com.example.tech_a_breath

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.tech_a_breath.service.MonitoringService
import com.example.tech_a_breath.ui.InterventionScreen
import com.example.tech_a_breath.ui.ListeningScreen
import com.example.tech_a_breath.ui.TriggerProtectionSettingsScreen
import com.example.tech_a_breath.ui.theme.TechABreathTheme

class MainActivity : ComponentActivity() {

    private var isServiceStarted = false

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else {
            true
        }
        val bluetoothGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
        } else {
            true
        }

        if (recordAudioGranted && notificationsGranted && bluetoothGranted) {
            startMonitoringService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow showing over lockscreen and wake up device for background interventions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContent {
            TechABreathTheme {
                var currentScreen by remember { 
                    mutableStateOf(if (TriggerManager.isProtectionActivated) "monitoring" else "settings") 
                }
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
                            TriggerManager.isProtectionActivated = true
                            currentScreen = "monitoring"
                            checkPermissionsAndStart()
                        })
                    } else {
                        ListeningScreen(
                            onOpenSettings = {
                                TriggerManager.isProtectionActivated = false
                                currentScreen = "settings"
                            },
                            onStopShield = {
                                stopMonitoringService()
                                TriggerManager.isProtectionActivated = false
                                currentScreen = "settings"
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        TriggerManager.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        TriggerManager.setAppForeground(false)
    }

    private fun checkPermissionsAndStart() {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            startMonitoringService()
        } else {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
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

    private fun stopMonitoringService() {
        val intent = Intent(this, MonitoringService::class.java)
        stopService(intent)
        isServiceStarted = false
    }
}
