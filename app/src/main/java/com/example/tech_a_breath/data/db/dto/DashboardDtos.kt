package com.example.tech_a_breath.data.db.dto

/** Total events over a period */
data class TotalCountDto(val total: Int)

/** Events grouped by trigger_id */
data class TriggerCountDto(val triggerId: Int, val total: Int)

/** Events grouped by ISO day-of-week (0=Sun … 6=Sat) */
data class DayOfWeekCountDto(val dayOfWeek: String, val total: Int)

/** Average event duration per trigger */
data class TriggerAvgDurationDto(val triggerId: Int, val avgDurationMs: Double)

/** Short / medium / long duration breakdown per trigger */
data class DurationBreakdownDto(
    val triggerId: Int,
    val shortCount: Int,
    val mediumCount: Int,
    val longCount: Int
)

// ── Monthly ──────────────────────────────────────────────────────────────────

/** Events grouped by ISO week number */
data class WeeklyCountDto(val weekNum: String, val total: Int)

/** Events grouped by hour of day (0–23) */
data class HourCountDto(val hour: Int, val total: Int)

/** One config-change record from trigger_config_history */
data class ConfigChangeDto(
    val triggerId: Int,
    val maskingPercentage: Int,
    val sensitivityLevel: Int,
    val changeSource: String,
    val changedAt: Long
)

// ── Effectiveness ─────────────────────────────────────────────────────────────

/** Rating/count per trigger + masking percentage — the scatter data */
data class EffectivenessPointDto(
    val triggerId: Int,
    val maskingPctApplied: Int,
    val avgRating: Double,
    val totalEvents: Int
)

/** Current masking percentage per active trigger */
data class CurrentMaskingDto(val triggerId: Int, val maskingPercentage: Int)
