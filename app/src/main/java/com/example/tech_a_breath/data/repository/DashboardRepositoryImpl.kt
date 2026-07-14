package com.example.tech_a_breath.data.repository

import com.example.tech_a_breath.data.*
import kotlinx.coroutines.flow.Flow

class DashboardRepositoryImpl(
    private val triggerDao: TriggerDao,
    private val weeklyDao: WeeklyDashboardDao,
    private val monthlyDao: MonthlyDashboardDao,
    private val effectivenessDao: EffectivenessDao
) : DashboardRepository {

    // ── Triggers ─────────────────────────────────────────────────────────────
    override fun getAllTriggers(): Flow<List<TriggerEntity>> =
        triggerDao.getAllTriggersFlow()

    // ── Weekly ────────────────────────────────────────────────────────────────
    override fun getWeeklyTotalEvents(): Flow<Int> =
        weeklyDao.getWeeklyTotalEvents()

    override fun getWeeklyCountByTrigger(): Flow<List<TriggerCountDto>> =
        weeklyDao.getWeeklyCountByTrigger()

    override fun getWeeklyCountByDayOfWeek(): Flow<List<DayOfWeekCountDto>> =
        weeklyDao.getWeeklyCountByDayOfWeek()

    override fun getWeeklyAvgDurationByTrigger(): Flow<List<TriggerAvgDurationDto>> =
        weeklyDao.getWeeklyAvgDurationByTrigger()

    override fun getWeeklyDurationBreakdown(): Flow<List<DurationBreakdownDto>> =
        weeklyDao.getWeeklyDurationBreakdown()

    // ── Monthly ───────────────────────────────────────────────────────────────
    override fun getMonthlyCountByWeek(): Flow<List<WeeklyCountDto>> =
        monthlyDao.getMonthlyCountByWeek()

    override fun getMonthlySensitiveDays(): Flow<List<DayOfWeekCountDto>> =
        monthlyDao.getMonthlySensitiveDays()

    override fun getMonthlyHourlyDistribution(): Flow<List<HourCountDto>> =
        monthlyDao.getMonthlyHourlyDistribution()

    override fun getMonthlyConfigChanges(): Flow<List<ConfigChangeDto>> =
        monthlyDao.getMonthlyConfigChanges()

    // ── Effectiveness ─────────────────────────────────────────────────────────
    override fun getEffectivenessData(): Flow<List<EffectivenessPointDto>> =
        effectivenessDao.getEffectivenessData()

    override fun getCurrentMasking(): Flow<List<CurrentMaskingDto>> =
        effectivenessDao.getCurrentMasking()

    // ── Writes ────────────────────────────────────────────────────────────────
    override suspend fun insertEvent(event: TriggerEventEntity) =
        weeklyDao.insertEvent(event)

    override suspend fun insertFeedback(feedback: EventFeedbackEntity) =
        weeklyDao.insertFeedback(feedback)

    override suspend fun updateMaskingPercentage(triggerId: Int, pct: Int) =
        triggerDao.updateMaskingPercentage(triggerId, pct)
}
