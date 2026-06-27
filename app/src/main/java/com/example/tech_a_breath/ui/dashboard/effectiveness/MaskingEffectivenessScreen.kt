package com.example.tech_a_breath.ui.dashboard.effectiveness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tech_a_breath.ui.dashboard.components.*
import com.example.tech_a_breath.ui.theme.*

@Composable
fun MaskingEffectivenessScreen(viewModel: MaskingEffectivenessViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        DashboardLoading()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        // ── Nudge card ────────────────────────────────────────────────────────
        state.nudge?.let { nudge ->
            item {
                NudgeInsightCard(
                    triggerName    = nudge.triggerId.toTriggerType().displayName(),
                    currentPct     = nudge.currentPct,
                    suggestedPct   = nudge.suggestedPct,
                    suggestedRating = nudge.suggestedRating
                )
            }
        }

        // ── Per-trigger effectiveness cards ───────────────────────────────────
        val triggerIds = state.effectivenessPoints.map { it.triggerId }.distinct()

        if (triggerIds.isEmpty()) {
            item {
                EmptyState(
                    "No effectiveness data yet.\n" +
                    "Data appears after 5+ rated events per masking level."
                )
            }
        } else {
            triggerIds.forEach { triggerId ->
                item {
                    val type = triggerId.toTriggerType()
                    val points = state.effectivenessPoints.filter { it.triggerId == triggerId }
                    val currentPct = state.currentMasking
                        .firstOrNull { it.triggerId == triggerId }?.maskingPercentage

                    SectionHeader(type.displayName())
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Rating bars per masking percentage
                            points.forEach { point ->
                                val isCurrent = point.maskingPctApplied == currentPct
                                EffectivenessRow(
                                    maskingPct  = point.maskingPctApplied,
                                    avgRating   = point.avgRating,
                                    totalEvents = point.totalEvents,
                                    isCurrent   = isCurrent,
                                    color       = type.color()
                                )
                            }

                            // "You are here" label
                            if (currentPct != null) {
                                Text(
                                    text = "★ Current setting: $currentPct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Amber500
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectivenessRow(
    maskingPct: Int,
    avgRating: Double,
    totalEvents: Int,
    isCurrent: Boolean,
    color: androidx.compose.ui.graphics.Color
) {
    val barFraction = (avgRating / 5.0).toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$maskingPct%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isCurrent) Amber500 else MaterialTheme.colorScheme.onSurface
                )
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Amber500.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "current",
                            style = MaterialTheme.typography.labelSmall,
                            color = Amber500
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RatingDots(avgRating)
                Text(
                    text = String.format("%.1f", avgRating),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "($totalEvents)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
        LinearProgressBar(
            fraction = barFraction,
            color    = color
        )
    }
}
