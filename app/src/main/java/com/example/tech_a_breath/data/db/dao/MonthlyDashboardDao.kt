package com.example.tech_a_breath.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.tech_a_breath.data.db.dto.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyDashboardDao {

    /**
     * Total events per ISO week number for the last 30 days.
     */
    @Query("""
        SELECT
          strftime('%W', datetime(detected_at / 1000, 'unixepoch')) as weekNum,
          COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-30 days') * 1000)
        GROUP BY weekNum
    """)
    fun getMonthlyCountByWeek(): Flow<List<WeeklyCountDto>>

    /**
     * Events per day-of-week for the last 30 days, ordered by frequency.
     */
    @Query("""
        SELECT
          strftime('%w', datetime(detected_at / 1000, 'unixepoch')) as dayOfWeek,
          COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-30 days') * 1000)
        GROUP BY dayOfWeek
        ORDER BY total DESC
    """)
    fun getMonthlySensitiveDays(): Flow<List<DayOfWeekCountDto>>

    /**
     * Events per hour of day (0–23) for the last 30 days.
     */
    @Query("""
        SELECT
          CAST(strftime('%H', datetime(detected_at / 1000, 'unixepoch')) AS INTEGER) as hour,
          COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-30 days') * 1000)
        GROUP BY hour
        ORDER BY hour ASC
    """)
    fun getMonthlyHourlyDistribution(): Flow<List<HourCountDto>>

    /**
     * All config changes for the last 30 days, oldest-first.
     * Lets the monthly screen render the masking-% trend line.
     */
    @Query("""
        SELECT
          trigger_id as triggerId,
          masking_percentage as maskingPercentage,
          sensitivity_level as sensitivityLevel,
          change_source as changeSource,
          changed_at as changedAt
        FROM trigger_config_history
        WHERE changed_at >= (strftime('%s', 'now', '-30 days') * 1000)
        ORDER BY changed_at ASC
    """)
    fun getMonthlyConfigChanges(): Flow<List<ConfigChangeDto>>
}
