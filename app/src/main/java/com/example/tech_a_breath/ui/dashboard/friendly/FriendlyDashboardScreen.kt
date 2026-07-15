package com.example.tech_a_breath.ui.dashboard.friendly

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tech_a_breath.ui.dashboard.components.DashboardLoading
import com.example.tech_a_breath.ui.theme.*

private val EMOJIS = listOf("😞", "😐", "🙂", "✨")

@Composable
fun FriendlyDashboardScreen(
    viewModel: FriendlyDashboardViewModel,
    onContinue: (rating: Int) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        DashboardLoading()
        return
    }

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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {

            // ── Botanical illustration placeholder ────────────────────────────────
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 110.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌿", fontSize = 48.sp)
            }

            // ── Heading ───────────────────────────────────────────────────────────
            Text(
                text = "Weekly Summary",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            // ── Summary sentence card ─────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = summaryText(state.weeklyCount, state.dominantMaskingPct),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 26.sp
                    ),
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }

            // ── Emoji picker card ─────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "How did masking feel this week?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        ),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EMOJIS.forEachIndexed { idx, emoji ->
                            val rating = idx + 1
                            val selected = state.selectedRating == rating
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Indigo500 else Color.White.copy(alpha = 0.05f))
                                    .clickable { viewModel.selectRating(rating) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 28.sp)
                            }
                        }
                    }
                }
            }

            // ── Continue button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    val rating = state.selectedRating ?: return@Button
                    viewModel.submitFeedback()
                    onContinue(rating)
                },
                enabled = state.selectedRating != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Indigo900,
                    disabledContainerColor = Color.White.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

private fun summaryText(count: Int, dominantPct: Int): String {
    val noun = if (count == 1) "moment" else "moments"
    val encouragement = when {
        dominantPct >= 80 -> "Great work protecting your calm."
        dominantPct >= 50 -> "Good progress this week."
        else              -> "Every step forward counts."
    }
    return "This week we detected $count $noun of noise.\n" +
           "In most of them, ${dominantPct}% masking was active.\n\n" +
           encouragement
}
