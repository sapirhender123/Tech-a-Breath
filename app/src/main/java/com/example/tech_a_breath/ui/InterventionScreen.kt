package com.example.tech_a_breath.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import com.example.tech_a_breath.R
import com.example.tech_a_breath.TriggerManager
import com.example.tech_a_breath.ui.components.CalmingWaveAnimation
import com.example.tech_a_breath.ui.theme.CalmingWave
import com.example.tech_a_breath.ui.theme.DeepBackground
import com.example.tech_a_breath.ui.theme.SoftText
import com.example.tech_a_breath.ui.theme.StopButton
import com.example.tech_a_breath.ui.theme.TechABreathTheme
import kotlinx.coroutines.delay

sealed class InterventionMode {
    data class Masking(val level: Float, val triggerName: String, val maskingMethod: String) : InterventionMode()
    object Breathing : InterventionMode()
    object PhoneCall : InterventionMode()
    object MusicActive : InterventionMode()
}

@Composable
fun InterventionScreen(
    mode: InterventionMode,
    onStop: () -> Unit
) {
    var timeLeftSeconds by remember { mutableStateOf(0) }
    val isTimerActive = timeLeftSeconds > 0

    // Timer logic
    LaunchedEffect(timeLeftSeconds) {
        if (timeLeftSeconds > 0) {
            delay(1000L)
            timeLeftSeconds -= 1
            if (timeLeftSeconds == 0) {
                onStop()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // Dynamic Calming Wave Animation
        CalmingWaveAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.Center)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Text Content based on mode
            InterventionContent(mode)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                if (isTimerActive) {
                    Text(
                        text = "Masking will end in ${timeLeftSeconds / 60}:${String.format("%02d", timeLeftSeconds % 60)}",
                        color = CalmingWave,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = "Keep masking for:",
                    color = SoftText.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(1, 3, 5).forEach { mins ->
                        OutlinedButton(
                            onClick = {
                                timeLeftSeconds = mins * 60
                                TriggerManager.setManualLock(true)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SoftText
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(CalmingWave, SoftText))
                            )
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${mins}m")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Large Stop Button - Redesigned to be more noticeable
                Button(
                    onClick = {
                        timeLeftSeconds = 0
                        TriggerManager.setManualLock(false)
                        onStop()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StopButton,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "STOP MASKING",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
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
                primaryText = "Trigger ${mode.triggerName} detected"
                secondaryText = "Activating ${mode.maskingMethod} (${(mode.level * 100).toInt()}% masking)"
            }
            InterventionMode.Breathing -> {
                primaryText = stringResource(R.string.breathe_together)
                secondaryText = stringResource(R.string.inhale_exhale)
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
            color = SoftText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = secondaryText,
            color = CalmingWave,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaskingPreview() {
    TechABreathTheme {
        InterventionScreen(mode = InterventionMode.Masking(0.8f, "Motorcycle", "White Noise"), onStop = {})
    }
}

@Preview(showBackground = true)
@Composable
fun BreathingPreview() {
    TechABreathTheme {
        InterventionScreen(mode = InterventionMode.Breathing, onStop = {})
    }
}
