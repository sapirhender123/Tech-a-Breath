package com.example.tech_a_breath.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import com.example.tech_a_breath.ui.theme.CalmingWave
import kotlin.math.sin

@Composable
fun CalmingWaveAnimation(
    modifier: Modifier = Modifier,
    waveColor: Color = CalmingWave,
    amplitude: Float = 40f,
    durationMillis: Int = 4000
) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
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
            val y = centerY + (sineValue * amplitude).toFloat()
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    waveColor.copy(alpha = 0.3f),
                    Color.Transparent
                )
            ),
            style = Fill
        )
    }
}
