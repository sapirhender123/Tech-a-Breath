package com.example.tech_a_breath.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.ui.theme.*

// ── Trigger helpers ───────────────────────────────────────────────────────────

/** Maps a trigger_id (1-based, matches the seeded triggers table) to a TriggerType. */
fun Int.toTriggerType(): TriggerType = when (this) {
    1 -> TriggerType.MOTORCYCLE
    2 -> TriggerType.DOG_BARK
    3 -> TriggerType.SIREN
    4 -> TriggerType.FIREWORK
    else -> TriggerType.UNKNOWN
}

/** Returns the brand color for this trigger — consistent across all three screens. */
fun TriggerType.color(): Color = when (this) {
    TriggerType.SIREN      -> TriggerSiren
    TriggerType.DOG_BARK   -> TriggerDogBark
    TriggerType.MOTORCYCLE -> TriggerMotorcycle
    TriggerType.FIREWORK   -> TriggerFirework
    TriggerType.BABY_CRYING -> Color(0xFF9575CD) // Purple for Baby Crying
    TriggerType.UNKNOWN    -> TriggerUnknown
}

/** Human-readable display name. */
fun TriggerType.displayName(): String = when (this) {
    TriggerType.SIREN      -> "Siren"
    TriggerType.DOG_BARK   -> "Dog Bark"
    TriggerType.MOTORCYCLE -> "Motorcycle"
    TriggerType.FIREWORK   -> "Firework"
    TriggerType.BABY_CRYING -> "Baby Crying"
    TriggerType.UNKNOWN    -> "Unknown"
}

// ── Stat Card ─────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            )
        }
    }
}

// ── Section Header ─────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

// ── Trigger Legend Row ─────────────────────────────────────────────────────────

@Composable
fun TriggerLegendRow(triggerId: Int, count: Int, total: Int, modifier: Modifier = Modifier) {
    val type = triggerId.toTriggerType()
    val fraction = if (total > 0) count.toFloat() / total else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(type.color())
                )
                Text(
                    text = type.displayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = type.color()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressBar(fraction = fraction, color = type.color())
    }
}

// ── Linear Progress Bar ────────────────────────────────────────────────────────

@Composable
fun LinearProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

// ── Vertical Bar Chart ─────────────────────────────────────────────────────────

/**
 * A simple Canvas-free bar chart drawn with Compose boxes.
 * [entries] is a list of (label, value) pairs.
 * [barColor] can be overridden per entry via [barColors].
 */
@Composable
fun VerticalBarChart(
    entries: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barColors: List<Color>? = null,
    maxValue: Int = entries.maxOfOrNull { it.second } ?: 1
) {
    if (entries.isEmpty()) {
        EmptyState("No data yet")
        return
    }
    val resolvedMax = maxValue.coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            entries.forEachIndexed { idx, (_, value) ->
                val fraction = value.toFloat() / resolvedMax
                val color = barColors?.getOrNull(idx) ?: barColor
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    if (value > 0) {
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(color)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            entries.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ── Nudge Insight Card ─────────────────────────────────────────────────────────

@Composable
fun NudgeInsightCard(
    triggerName: String,
    currentPct: Int,
    suggestedPct: Int,
    suggestedRating: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Amber100)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡  Masking Insight",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Teal900
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "For $triggerName, users with masking at $suggestedPct% " +
                       "report an average rating of ${String.format("%.1f", suggestedRating)}/5 — " +
                       "higher than your current $currentPct%.",
                style = MaterialTheme.typography.bodySmall,
                color = Teal800,
                lineHeight = 18.sp
            )
        }
    }
}

// ── Rating Dots ───────────────────────────────────────────────────────────────

@Composable
fun RatingDots(rating: Double, max: Int = 5, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(max) { idx ->
            val filled = idx < rating.toInt()
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (filled) Amber500 else MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Loading Placeholder ────────────────────────────────────────────────────────

@Composable
fun DashboardLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
