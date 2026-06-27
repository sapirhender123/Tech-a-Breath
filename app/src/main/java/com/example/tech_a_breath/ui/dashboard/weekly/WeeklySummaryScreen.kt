package com.example.tech_a_breath.ui.dashboard.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tech_a_breath.ui.dashboard.components.*
import com.example.tech_a_breath.ui.theme.Amber500

private val DAY_LABELS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
fun WeeklySummaryScreen(viewModel: WeeklySummaryViewModel) {
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

        // ── Hero stat ─────────────────────────────────────────────────────────
        item {
            StatCard(
                label = "Events this week",
                value = state.totalEvents.toString(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ── By trigger ────────────────────────────────────────────────────────
        item {
            SectionHeader("By Trigger Type")
            if (state.countByTrigger.isEmpty()) {
                EmptyState("No events recorded yet")
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.countByTrigger.forEach { dto ->
                            TriggerLegendRow(
                                triggerId = dto.triggerId,
                                count     = dto.total,
                                total     = state.totalEvents
                            )
                        }
                    }
                }
            }
        }

        // ── Events by day of week ─────────────────────────────────────────────
        item {
            SectionHeader("Events by Day of Week")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Build a full 7-day list, filling zeros for missing days
                    val dayMap = state.countByDay.associate { it.dayOfWeek to it.total }
                    val entries = (0..6).map { idx ->
                        DAY_LABELS[idx] to (dayMap[idx.toString()] ?: 0)
                    }
                    VerticalBarChart(
                        entries = entries,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ── Average duration ──────────────────────────────────────────────────
        if (state.avgDurationByTrigger.isNotEmpty()) {
            item {
                SectionHeader("Avg. Duration (seconds)")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.avgDurationByTrigger.forEach { dto ->
                            val type = dto.triggerId.toTriggerType()
                            val secs = (dto.avgDurationMs / 1000).toInt()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = type.displayName(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${secs}s",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = type.color()
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Duration breakdown ────────────────────────────────────────────────
        if (state.durationBreakdown.isNotEmpty()) {
            item {
                SectionHeader("Duration Breakdown")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.durationBreakdown.forEach { dto ->
                            val type = dto.triggerId.toTriggerType()
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = type.displayName(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val total = dto.shortCount + dto.mediumCount + dto.longCount
                                if (total > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        SegmentBar(
                                            fraction = dto.shortCount.toFloat() / total,
                                            color = MaterialTheme.colorScheme.primary,
                                            label = "< 10s"
                                        )
                                        SegmentBar(
                                            fraction = dto.mediumCount.toFloat() / total,
                                            color = Amber500,
                                            label = "10–30s"
                                        )
                                        SegmentBar(
                                            fraction = dto.longCount.toFloat() / total,
                                            color = MaterialTheme.colorScheme.error,
                                            label = "> 30s"
                                        )
                                    }
                                }
                            }
                        }
                        // Legend
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LegendDot(MaterialTheme.colorScheme.primary, "< 10s")
                            LegendDot(Amber500, "10–30s")
                            LegendDot(MaterialTheme.colorScheme.error, "> 30s")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.SegmentBar(fraction: Float, color: Color, label: String) {
    if (fraction > 0f) {
        Box(
            modifier = Modifier
                .weight(fraction.coerceAtLeast(0.001f))
                .fillMaxHeight()
                .background(color)
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
