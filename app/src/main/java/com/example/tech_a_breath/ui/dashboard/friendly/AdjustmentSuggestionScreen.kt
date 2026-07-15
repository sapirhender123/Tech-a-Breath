package com.example.tech_a_breath.ui.dashboard.friendly

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tech_a_breath.ui.dashboard.components.LinearProgressBar
import com.example.tech_a_breath.ui.theme.*

@Composable
fun AdjustmentSuggestionScreen(
    rating: Int,
    onBack: () -> Unit,
    onApply: () -> Unit,
    onKeep: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Indigo900, Indigo800)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {

            // ── Back arrow ────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Heading ───────────────────────────────────────────────────────────
            Text(
                text = "Adjustment\nSuggestion",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 36.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Based on your feedback from last week, it seems we can try a slight reduction.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Content — switches on rating ──────────────────────────────────────
            if (rating >= 3) {
                SuggestionCard(onApply = onApply, onKeep = onKeep)
            } else {
                SupportiveCard(onKeep = onKeep)
            }
        }
    }
}

// ── Suggestion card (rating 3–4) ──────────────────────────────────────────────

@Composable
private fun SuggestionCard(onApply: () -> Unit, onKeep: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(36.dp)
        )

        // Title
        Text(
            text = "Automatic Volume Update",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        // Body
        Text(
            text = "Would you like us to slightly lower the overall masking volume for next week? " +
                   "The process is gradual, and you can always change it back in settings.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bars — current vs recommended (~10% lower)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressBarRow(label = "Current Volume", fraction = 1.0f, color = Color.White.copy(alpha = 0.2f))
            ProgressBarRow(label = "Recommended (New)", fraction = 0.9f, color = Indigo500)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Buttons
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Indigo900)
        ) {
            Text(
                text = "Apply",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        OutlinedButton(
            onClick = onKeep,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text(
                text = "Keep as is",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// ── Supportive card (rating 1–2) ───────────────────────────────────────────────

@Composable
private fun SupportiveCard(onKeep: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(24.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "🌱", fontSize = 48.sp)
        Text(
            text = "Thank you for sharing.",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Keep going at your own pace. Your current settings will stay as they are.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Button(
            onClick = onKeep,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Indigo900)
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

@Composable
private fun ProgressBarRow(
    label: String,
    fraction: Float,
    color: androidx.compose.ui.graphics.Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f))
        )
        LinearProgressBar(
            fraction = fraction, 
            color = color, 
            height = 8.dp,
            trackColor = Color.White.copy(alpha = 0.05f)
        )
    }
}
