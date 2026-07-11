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
import com.example.tech_a_breath.R
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.ui.components.CalmingWaveAnimation
import com.example.tech_a_breath.ui.theme.CalmingWave
import com.example.tech_a_breath.ui.theme.DeepBackground
import com.example.tech_a_breath.ui.theme.SoftText
import com.example.tech_a_breath.ui.theme.StopButton
import com.example.tech_a_breath.ui.theme.TechABreathTheme

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
            Spacer(modifier = Modifier.height(48.dp))

            // Text Content based on mode
            InterventionContent(mode)

            // Large Stop Button
            Button(
                onClick = onStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StopButton,
                    contentColor = SoftText
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.stop_action),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
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
                "Motorcycle", 
                "White Noise", 
                TriggerType.MOTORCYCLE,
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
