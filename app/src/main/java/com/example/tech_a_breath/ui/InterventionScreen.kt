package com.example.tech_a_breath.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Waves
import com.example.tech_a_breath.R
import com.example.tech_a_breath.TriggerManager
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.ui.components.CalmingWaveAnimation
import com.example.tech_a_breath.ui.theme.CalmingWave
import com.example.tech_a_breath.ui.theme.DeepBackground
import com.example.tech_a_breath.ui.theme.SoftText
import com.example.tech_a_breath.ui.theme.StopButton
import com.example.tech_a_breath.ui.theme.TechABreathTheme
import kotlinx.coroutines.delay

sealed class InterventionMode {
    data class Masking(
        val level: Float,
        val triggerName: String,
        val maskingMethod: String,
        val triggerType: TriggerType,
        val responseType: String
    ) : InterventionMode()
    object PhoneCall : InterventionMode()
    object MusicActive : InterventionMode()
}

@Composable
fun InterventionScreen(
    mode: InterventionMode,
    onStop: () -> Unit
) {
    val manualLockTimeLeft by TriggerManager.manualLockTimeLeft.collectAsState()
    val isTimerActive = manualLockTimeLeft > 0

    var sliderValue by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B), // Deep Indigo
                        Color(0xFF312E81), // Indigo 900
                        Color(0xFF1E293B)  // Slate 800
                    )
                )
            )
    ) {
        // More sophisticated background design with soft blurry shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = size.width * 0.7f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.15f)
            )
            drawCircle(
                color = Color(0xFF6366F1).copy(alpha = 0.08f),
                radius = size.width * 0.5f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.85f)
            )
        }

        // Layered Calming Waves at the bottom
        CalmingWaveAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter),
            waveColor = CalmingWave.copy(alpha = 0.12f),
            amplitude = 40f,
            durationMillis = 9000
        )
        
        CalmingWaveAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.BottomCenter),
            waveColor = CalmingWave.copy(alpha = 0.1f),
            amplitude = 30f,
            durationMillis = 10000
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                InterventionContent(mode)
            }

            // Central Calming Element
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF312E81), // Deep Purple/Indigo matching background
                    shadowElevation = 12.dp,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Inner glow effect
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(CalmingWave.copy(alpha = 0.1f), Color.Transparent),
                                    center = center,
                                    radius = size.width / 2
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(64.dp)
                        )
                        
                        // Moving waves overlay on the icon
                        CalmingWaveAnimation(
                            modifier = Modifier.size(90.dp),
                            waveColor = Color.White.copy(alpha = 0.3f),
                            amplitude = 12f,
                            durationMillis = 2500
                        )
                    }
                }
            }

            // Interactive Controls Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Slider with specific time labels
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = if (isTimerActive) "Masking sound active" else "Extend the silence",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = {
                            if (sliderValue > 0) {
                                TriggerManager.setManualLock(true, sliderValue.toInt() * 60)
                            }
                        },
                        valueRange = 0f..5f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (0..5).forEach { min ->
                            Text(
                                text = if (min == 0) "Now" else "${min}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (sliderValue.toInt() == min) Color.White else Color.White.copy(alpha = 0.4f),
                                fontWeight = if (sliderValue.toInt() == min) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Sophisticated Stop Button
                OutlinedButton(
                    onClick = {
                        TriggerManager.setManualLock(false)
                        onStop()
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(0.8f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "I feel safe now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

@Composable
fun InterventionContent(mode: InterventionMode) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val primaryText: String
        val secondaryText: String

        when (mode) {
            is InterventionMode.Masking -> {
                val triggerDisplayName = when (mode.triggerType) {
                    TriggerType.SIREN -> "Ambulance"
                    TriggerType.DOG_BARK -> "Dog Barking"
                    TriggerType.BABY_CRYING -> "Baby Crying"
                    else -> mode.triggerName
                }
                primaryText = "Acoustic Shield Active"
                secondaryText = "Masking $triggerDisplayName"
            }
            InterventionMode.PhoneCall -> {
                primaryText = stringResource(R.string.silent_support)
                secondaryText = stringResource(R.string.no_voice_during_call)
            }
            InterventionMode.MusicActive -> {
                primaryText = stringResource(R.string.minimal_intervention)
                secondaryText = stringResource(R.string.music_not_stopped)
            }
        }

        Text(
            text = primaryText,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = secondaryText,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaskingPreview() {
    TechABreathTheme {
        InterventionScreen(
            mode = InterventionMode.Masking(
                0.8f, 
                "Ambulance", 
                "White Noise", 
                TriggerType.SIREN,
                "white_noise"
            ),
            onStop = {}
        )
    }
}
