package com.example.tech_a_breath.data.repository

import com.example.tech_a_breath.data.db.dao.*
import com.example.tech_a_breath.data.db.dto.*
import com.example.tech_a_breath.data.db.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Single entry-point for all dashboard data.
 * ViewModels depend on this interface — swap the real impl for a fake in tests.
 */
interface DashboardRepository {

    // ── Triggers ─────────────────────────────────────────────────────────────
    fun getAllTriggers(): Flow<List<TriggerEntity>>

    // ── Weekly ────────────────────────────────────────────────────────────────
    fun getWeeklyTotalEvents(): Flow<Int>
    fun getWeeklyCountByTrigger(): Flow<List<TriggerCountDto>>
    fun getWeeklyCountByDayOfWeek(): Flow<List<DayOfWeekCountDto>>
    fun getWeeklyAvgDurationByTrigger(): Flow<List<TriggerAvgDurationDto>>
    fun getWeeklyDurationBreakdown(): Flow<List<DurationBreakdownDto>>

    // ── Monthly ───────────────────────────────────────────────────────────────
    fun getMonthlyCountByWeek(): Flow<List<WeeklyCountDto>>
    fun getMonthlySensitiveDays(): Flow<List<DayOfWeekCountDto>>
    fun getMonthlyHourlyDistribution(): Flow<List<HourCountDto>>
    fun getMonthlyConfigChanges(): Flow<List<ConfigChangeDto>>

    // ── Effectiveness ─────────────────────────────────────────────────────────
    fun getEffectivenessData(): Flow<List<EffectivenessPointDto>>
    fun getCurrentMasking(): Flow<List<CurrentMaskingDto>>

    // ── Writes ────────────────────────────────────────────────────────────────
    suspend fun insertEvent(event: TriggerEventEntity)
    suspend fun insertFeedback(feedback: EventFeedbackEntity)
}
