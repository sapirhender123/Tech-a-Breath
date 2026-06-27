package com.example.tech_a_breath.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tech_a_breath.data.db.dto.*
import com.example.tech_a_breath.data.db.entity.TriggerEventEntity
import com.example.tech_a_breath.data.db.entity.EventFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyDashboardDao {

    // ── Write ─────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TriggerEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<TriggerEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: EventFeedbackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFeedback(feedback: List<EventFeedbackEntity>)

    /** One-shot (non-Flow) count — used by MockDataSeeder to check if DB is empty. */
    @Query("SELECT COUNT(*) FROM trigger_events")
    suspend fun getWeeklyTotalEventsOnce(): Int

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Total events in the last 7 days.
     * strftime('%s','now','-7 days') returns seconds → multiply by 1000 for millis.
     */
    @Query("""
        SELECT COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-7 days') * 1000)
    """)
    fun getWeeklyTotalEvents(): Flow<Int>

    /**
     * Event count per trigger for the last 7 days.
     */
    @Query("""
        SELECT trigger_id as triggerId, COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-7 days') * 1000)
        GROUP BY trigger_id
    """)
    fun getWeeklyCountByTrigger(): Flow<List<TriggerCountDto>>

    /**
     * Events per day-of-week for the last 7 days.
     * day_of_week: '0'=Sun, '1'=Mon … '6'=Sat
     */
    @Query("""
        SELECT
          strftime('%w', datetime(detected_at / 1000, 'unixepoch')) as dayOfWeek,
          COUNT(*) as total
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-7 days') * 1000)
        GROUP BY dayOfWeek
    """)
    fun getWeeklyCountByDayOfWeek(): Flow<List<DayOfWeekCountDto>>

    /**
     * Average event duration per trigger for the last 7 days.
     */
    @Query("""
        SELECT trigger_id as triggerId, AVG(event_duration_ms) as avgDurationMs
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-7 days') * 1000)
          AND event_duration_ms IS NOT NULL
        GROUP BY trigger_id
    """)
    fun getWeeklyAvgDurationByTrigger(): Flow<List<TriggerAvgDurationDto>>

    /**
     * Short (<10 s), medium (10–30 s), long (>30 s) counts per trigger for the last 7 days.
     */
    @Query("""
        SELECT
          trigger_id as triggerId,
          SUM(CASE WHEN event_duration_ms < 10000 THEN 1 ELSE 0 END) as shortCount,
          SUM(CASE WHEN event_duration_ms BETWEEN 10000 AND 30000 THEN 1 ELSE 0 END) as mediumCount,
          SUM(CASE WHEN event_duration_ms > 30000 THEN 1 ELSE 0 END) as longCount
        FROM trigger_events
        WHERE detected_at >= (strftime('%s', 'now', '-7 days') * 1000)
          AND event_duration_ms IS NOT NULL
        GROUP BY trigger_id
    """)
    fun getWeeklyDurationBreakdown(): Flow<List<DurationBreakdownDto>>
}
