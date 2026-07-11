package com.example.tech_a_breath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tech_a_breath.TriggerManager
import com.example.tech_a_breath.TriggerSettingData
import com.example.tech_a_breath.ai.TriggerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerProtectionSettingsScreen(onStartProtection: () -> Unit) {
    // Collect the settings from the Manager
    val triggers = remember { TriggerManager.settings }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Your Acoustic Shield",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select which sounds you'd like me to soften for you today.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                )
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onStartProtection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Activate Protection", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(triggers, key = { it.triggerId }) { trigger ->
                TriggerCard(trigger)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerCard(trigger: TriggerSettingData) {
    // We use derived values or local state that updates when the trigger object changes
    var maskingLevel by remember(trigger.triggerId) { mutableStateOf(trigger.maskingLevel) }
    var isEnabled by remember(trigger.triggerId) { mutableStateOf(trigger.isEnabled) }
    var responseType by remember(trigger.triggerId) { mutableStateOf(trigger.responseType) }
    
    val icon = when(trigger.type) {
        TriggerType.SIREN -> Icons.Default.Campaign
        TriggerType.DOG_BARK -> Icons.Default.Pets
        TriggerType.MOTORCYCLE -> Icons.Default.TwoWheeler
        TriggerType.FIREWORK -> Icons.Default.Celebration
        else -> Icons.Default.VolumeUp
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trigger.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                        )
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        isEnabled = it
                        TriggerManager.updateSetting(trigger.triggerId, maskingLevel, it, responseType)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Protection Strength",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Slider(
                    value = maskingLevel,
                    onValueChange = { 
                        maskingLevel = it
                        TriggerManager.updateSetting(trigger.triggerId, it, isEnabled, responseType)
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Choose a masking sound",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                val responseOptions = listOf(
                    "white_noise" to "White Noise",
                    "calming_music" to "Calming Music"
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    responseOptions.forEach { (type, label) ->
                        FilterChip(
                            selected = responseType == type,
                            onClick = { 
                                responseType = type
                                TriggerManager.updateSetting(trigger.triggerId, maskingLevel, isEnabled, type)
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }
}

