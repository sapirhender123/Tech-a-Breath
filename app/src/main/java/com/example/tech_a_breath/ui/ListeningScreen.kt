package com.example.tech_a_breath.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tech_a_breath.HeadphoneManager
import com.example.tech_a_breath.ui.components.CalmingWaveAnimation
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ListeningScreen(onOpenSettings: () -> Unit, onStopShield: () -> Unit) {
    val isHeadsetConnected by HeadphoneManager.isHeadsetConnected.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var statusText by remember { mutableStateOf("Activating the shield...") }
    LaunchedEffect(Unit) {
        delay(3000)
        statusText = "Shield is active"
    }

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
        // Immersive "Designed" Background with glowing Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6366F1).copy(alpha = 0.15f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.3f),
                    radius = size.width * 0.8f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF10B981).copy(alpha = 0.1f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.7f),
                    radius = size.width * 0.6f
                )
            )
        }

        // Subtly moving waves in background
        CalmingWaveAnimation(
            modifier = Modifier.fillMaxSize(),
            waveColor = Color.White.copy(alpha = 0.05f),
            amplitude = 25f,
            durationMillis = 15000
        )

        // Main Content Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar - Minimalist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isHeadsetConnected) Icons.Default.Headset else Icons.Default.HeadsetOff,
                            contentDescription = null,
                            tint = if (isHeadsetConnected) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHeadsetConnected) "Connected" else "Connect headphones",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Central Relaxation Focus
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = breatheScale
                    scaleY = breatheScale
                }
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.size(200.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        
                        // Listening waves overlay on the shield
                        CalmingWaveAnimation(
                            modifier = Modifier.size(120.dp),
                            waveColor = Color.White.copy(alpha = 0.2f),
                            amplitude = 10f,
                            durationMillis = 5000
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                AnimatedContent(
                    targetState = statusText,
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = fadeIn(animationSpec = tween(1500)),
                            initialContentExit = fadeOut(animationSpec = tween(1500))
                        )
                    }
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Actions & Status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "The world is quiet. You are safe.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.ExtraLight,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                OutlinedButton(
                    onClick = onStopShield,
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Stop Shield",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }

        // Subtle warning if no triggers
        val anyTriggerEnabled = com.example.tech_a_breath.TriggerManager.settings.any { it.isEnabled }
        if (!anyTriggerEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "No sounds selected for protection",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
