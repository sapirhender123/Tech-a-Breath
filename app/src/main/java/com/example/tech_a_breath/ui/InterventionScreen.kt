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
import com.example.tech_a_breath.ui.theme.CalmingWave
import com.example.tech_a_breath.ui.theme.DeepBackground
import com.example.tech_a_breath.ui.theme.SoftText
import com.example.tech_a_breath.ui.theme.StopButton
import com.example.tech_a_breath.ui.theme.TechABreathTheme
import kotlin.math.sin

sealed class InterventionMode {
    data class Masking(val level: Float, val label: String) : InterventionMode()
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
                primaryText = "${mode.label} noise detected"
                secondaryText = "Masking ${(mode.level * 100).toInt()}% active"
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

@Composable
fun CalmingWaveAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        val path = Path()
        path.moveTo(0f, centerY)

        for (x in 0..width.toInt() step 5) {
            val relativeX = x.toFloat() / width
            val sineValue = sin(relativeX * 2 * Math.PI + phase)
            val y = centerY + (sineValue * 40).toFloat()
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    CalmingWave.copy(alpha = 0.3f),
                    Color.Transparent
                )
            ),
            style = Fill
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaskingPreview() {
    TechABreathTheme {
        InterventionScreen(mode = InterventionMode.Masking(0.8f, "Motorcycle"), onStop = {})
    }
}

@Preview(showBackground = true)
@Composable
fun BreathingPreview() {
    TechABreathTheme {
        InterventionScreen(mode = InterventionMode.Breathing, onStop = {})
    }
}
