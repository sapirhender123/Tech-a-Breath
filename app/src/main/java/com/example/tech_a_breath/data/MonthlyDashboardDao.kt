package com.example.tech_a_breath.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyDashboardDao {

    @Query("""
        SELECT
          strftime('%W', datetime(detected_at / 1000, 'unixepoch')) as weekNum,
          COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-30 days') * 1000)
        GROUP BY weekNum
    """)
    fun getMonthlyCountByWeek(): Flow<List<WeeklyCountDto>>

    @Query("""
        SELECT
          strftime('%w', datetime(detected_at / 1000, 'unixepoch')) as dayOfWeek,
          COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-30 days') * 1000)
        GROUP BY dayOfWeek
    """)
    fun getMonthlySensitiveDays(): Flow<List<DayOfWeekCountDto>>

    @Query("""
        SELECT
          CAST(strftime('%H', datetime(detected_at / 1000, 'unixepoch')) AS INTEGER) as hour,
          COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-30 days') * 1000)
        GROUP BY hour
    """)
    fun getMonthlyHourlyDistribution(): Flow<List<HourCountDto>>

    @Query("""
        SELECT
          trigger_id as triggerId,
          masking_percentage as maskingPercentage,
          sensitivity_level as sensitivityLevel,
          change_source as changeSource,
          changed_at as changedAt
        FROM trigger_config_history
        WHERE changed_at >= (strftime('%s', 'now', '-30 days') * 1000)
        ORDER BY changed_at DESC
    """)
    fun getMonthlyConfigChanges(): Flow<List<ConfigChangeDto>>
}
