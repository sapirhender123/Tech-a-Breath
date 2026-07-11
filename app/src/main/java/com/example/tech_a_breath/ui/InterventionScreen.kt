package com.example.tech_a_breath.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import com.example.tech_a_breath.R
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.TriggerManager
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

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

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
                    Box(
                        modifier = Modifier
                            .background(
                                CalmingWave.copy(alpha = 0.1f),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Shielding active • Safe space",
                            color = CalmingWave.copy(alpha = pulseAlpha),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "Would you like to rest for a bit?",
                    color = SoftText.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val options = listOf(
                        1 to "Just a moment",
                        3 to "Take a breath",
                        5 to "Stay with me"
                    )
                    options.forEach { (mins, label) ->
                        OutlinedButton(
                            onClick = {
                                timeLeftSeconds = mins * 60
                                TriggerManager.setManualLock(true)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SoftText,
                                containerColor = if (timeLeftSeconds == mins * 60) CalmingWave.copy(alpha = 0.1f) else Color.Transparent
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(
                                    if (timeLeftSeconds == mins * 60) 
                                        listOf(CalmingWave, CalmingWave) 
                                    else 
                                        listOf(CalmingWave.copy(alpha = 0.4f), SoftText.copy(alpha = 0.2f))
                                )
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Friendly Circular Stop Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            timeLeftSeconds = 0
                            TriggerManager.setManualLock(false)
                            onStop()
                        },
                        modifier = Modifier.size(150.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StopButton,
                            contentColor = Color.White
                        ),
                        shape = CircleShape,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "I feel\nsafe now",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
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
        InterventionScreen(
            mode = InterventionMode.Masking(
                0.8f, 
                "Baby Crying", 
                "White Noise", 
                TriggerType.BABY_CRYING,
                "white_noise"
            ),
            onStop = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BreathingPreview() {
    TechABreathTheme {
        InterventionScreen(mode = InterventionMode.Breathing, onStop = {})
    }
}
