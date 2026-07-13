package com.example.tech_a_breath.data

data class TotalCountDto(val total: Int)

data class TriggerCountDto(val triggerId: Int, val total: Int)

data class DayOfWeekCountDto(val dayOfWeek: String, val total: Int)

data class TriggerAvgDurationDto(val triggerId: Int, val avgDurationMs: Double)

data class DurationBreakdownDto(
    val triggerId: Int,
    val shortCount: Int,
    val mediumCount: Int,
    val longCount: Int
)

data class WeeklyCountDto(val weekNum: String, val total: Int)

data class HourCountDto(val hour: Int, val total: Int)

data class ConfigChangeDto(
    val triggerId: Int,
    val maskingPercentage: Int,
    val sensitivityLevel: Int,
    val changeSource: String,
    val changedAt: Long
)

data class EffectivenessPointDto(
    val triggerId: Int,
    val maskingPctApplied: Int,
    val avgRating: Double,
    val totalEvents: Int
)

data class CurrentMaskingDto(val triggerId: Int, val maskingPercentage: Int)
