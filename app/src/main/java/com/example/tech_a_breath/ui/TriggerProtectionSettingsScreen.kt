package com.example.tech_a_breath.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B), // Midnight Indigo
                        Color(0xFF312E81)
                    )
                )
            )
    ) {
        // Decorative background elements
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = size.width * 0.6f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, 0f)
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 40.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Your Acoustic Shield",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Customize how the world sounds to you.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 24.sp
                        )
                    )
                }
            },
            bottomBar = {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Button(
                        onClick = onStartProtection,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1E1B4B)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Activate Protection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = innerPadding.calculateTopPadding(),
                    bottom = 120.dp
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerCard(trigger: TriggerSettingData) {
    // We use derived values or local state that updates when the trigger object changes
    var maskingLevel by remember(trigger.triggerId) { mutableStateOf(trigger.maskingLevel) }
    var isEnabled by remember(trigger.triggerId) { mutableStateOf(trigger.isEnabled) }
    var responseType by remember(trigger.triggerId) { mutableStateOf(trigger.responseType) }
    var minDuration by remember(trigger.triggerId) { mutableStateOf(trigger.minMaskingDuration.toFloat()) }
    var sensitivityLevel by remember(trigger.triggerId) { mutableStateOf(trigger.sensitivityLevel) }
    
    val icon = when(trigger.type) {
        TriggerType.SIREN -> Icons.Default.Campaign
        TriggerType.DOG_BARK -> Icons.Default.Pets
        TriggerType.BABY_CRYING -> Icons.Default.ChildCare
        else -> Icons.Default.VolumeUp
    }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                Color.White.copy(alpha = 0.15f)
            else 
                Color.White.copy(alpha = 0.05f)
        ),
        border = if (isEnabled) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null,
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
                    color = if (isEnabled) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trigger.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )
                    Text(
                        text = if (isEnabled) "Shielding active" else "Paused",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        isEnabled = it
                        TriggerManager.updateSetting(trigger.triggerId, maskingLevel, it, responseType, minMaskingDuration = minDuration.toInt(), sensitivityLevel = sensitivityLevel)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.3f),
                        uncheckedTrackColor = Color.Transparent
                    )
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Masking Volume",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${(maskingLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }

                Slider(
                    value = maskingLevel,
                    onValueChange = { 
                        maskingLevel = it
                        TriggerManager.updateSetting(trigger.triggerId, it, isEnabled, responseType, minMaskingDuration = minDuration.toInt(), sensitivityLevel = sensitivityLevel)
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Min Masking Duration: ${minDuration.toInt()}s",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Slider(
                    value = minDuration,
                    onValueChange = { 
                        minDuration = it
                        TriggerManager.updateSetting(trigger.triggerId, maskingLevel, isEnabled, responseType, minMaskingDuration = it.toInt(), sensitivityLevel = sensitivityLevel)
                    },
                    valueRange = 3f..60f,
                    steps = 57,
                    modifier = Modifier.padding(vertical = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Masking Sound",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                val responseOptions = listOf(
                    Triple("white_noise", "White Noise", Icons.Default.Air),
                    Triple("brown_noise", "Brown Noise", Icons.Default.Waves),
                    Triple("calming_music", "Calm Music", Icons.Default.MusicNote)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    responseOptions.forEach { (type, label, icon) ->
                        val selected = responseType == type
                        Surface(
                            onClick = {
                                responseType = type
                                TriggerManager.updateSetting(trigger.triggerId, maskingLevel, isEnabled, type, minMaskingDuration = minDuration.toInt(), sensitivityLevel = sensitivityLevel)
                            },
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selected) Color(0xFF1E1B4B) else Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color(0xFF1E1B4B) else Color.White,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
