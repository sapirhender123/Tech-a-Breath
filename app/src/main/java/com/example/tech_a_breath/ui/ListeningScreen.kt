package com.example.tech_a_breath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tech_a_breath.HeadphoneManager
import com.example.tech_a_breath.ui.components.CalmingWaveAnimation

@Composable
fun ListeningScreen(
    onOpenSettings: () -> Unit,
    onStopShield: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenFriendlyDashboard: () -> Unit,
) {
    val isHeadsetConnected by HeadphoneManager.isHeadsetConnected.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenDashboard
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Dashboard",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Info, // Consider using a headset icon if available
                contentDescription = "Headphones Status",
                tint = if (isHeadsetConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = if (isHeadsetConnected) "Connected" else "No Headphones",
                style = MaterialTheme.typography.labelSmall,
                color = if (isHeadsetConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onOpenSettings
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CalmingWaveAnimation(
                modifier = Modifier
                    .size(240.dp),
                amplitude = 20f,
                durationMillis = 5000
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Acoustic Shield Active",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "I'm listening and ready to protect your peace. You're safe to focus on what matters.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 28.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(2.dp)
                    ) {
                         CircularProgressIndicator(
                             strokeWidth = 2.dp,
                             color = MaterialTheme.colorScheme.primary
                         )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Monitoring environment...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onStopShield,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Stop Shield")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Friendly Dashboard Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenFriendlyDashboard,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("How was your week?")
                }

                OutlinedButton(
                    onClick = onOpenDashboard,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Open Dashboard")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Check if any triggers are active in the settings
            val anyTriggerEnabled = com.example.tech_a_breath.TriggerManager.settings.any { it.isEnabled }

            if (!anyTriggerEnabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Warning: No triggers selected. Please enable sounds you want to mask in the settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
