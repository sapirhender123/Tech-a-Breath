package com.example.tech_a_breath.ui.dashboard.monthly

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tech_a_breath.data.db.ChangeSource
import com.example.tech_a_breath.ui.dashboard.components.*
import com.example.tech_a_breath.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val DAY_LABELS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
fun MonthlySummaryScreen(viewModel: MonthlySummaryViewModel) {
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

        // ── Events by week ────────────────────────────────────────────────────
        item {
            SectionHeader("Events by Week")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (state.countByWeek.isEmpty()) {
                        EmptyState("No data yet")
                    } else {
                        VerticalBarChart(
                            entries = state.countByWeek.map { dto ->
                                "Wk ${dto.weekNum}" to dto.total
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ── Sensitive days ────────────────────────────────────────────────────
        item {
            SectionHeader("Most Active Days")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.sensitiveDays.isEmpty()) {
                        EmptyState("No data yet")
                    } else {
                        val maxDay = state.sensitiveDays.maxOf { it.total }
                        state.sensitiveDays.take(7).forEach { dto ->
                            val idx = dto.dayOfWeek.toIntOrNull() ?: 0
                            val label = DAY_LABELS.getOrElse(idx) { dto.dayOfWeek }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(36.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                LinearProgressBar(
                                    fraction = dto.total.toFloat() / maxDay,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = dto.total.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Hourly distribution ───────────────────────────────────────────────
        item {
            SectionHeader("Hourly Distribution")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (state.hourlyDistribution.isEmpty()) {
                        EmptyState("No data yet")
                    } else {
                        // Group into 4-hour buckets for readability
                        val buckets = (0..5).map { bucket ->
                            val from = bucket * 4
                            val to = from + 3
                            val label = "${from}h"
                            val count = state.hourlyDistribution
                                .filter { it.hour in from..to }
                                .sumOf { it.total }
                            label to count
                        }
                        VerticalBarChart(
                            entries = buckets,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // ── Config changes ────────────────────────────────────────────────────
        if (state.configChanges.isNotEmpty()) {
            item {
                SectionHeader("Masking Changes This Month")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.configChanges.takeLast(10).reversed().forEach { dto ->
                            ConfigChangeRow(dto)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigChangeRow(dto: com.example.tech_a_breath.data.db.dto.ConfigChangeDto) {
    val type = dto.triggerId.toTriggerType()
    val isNudge = dto.changeSource == ChangeSource.APP_NUDGE.dbValue
    val badgeColor = if (isNudge) Amber500 else MaterialTheme.colorScheme.primary
    val badgeLabel = if (isNudge) "App nudge" else "Manual"
    val dateStr = remember(dto.changedAt) {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(dto.changedAt))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(type.color())
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${type.displayName()} → ${dto.maskingPercentage}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = badgeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor
            )
        }
    }
}
