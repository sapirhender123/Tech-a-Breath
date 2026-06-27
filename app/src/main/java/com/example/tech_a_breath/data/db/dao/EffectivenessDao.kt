package com.example.tech_a_breath.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.tech_a_breath.data.db.dto.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EffectivenessDao {

    /**
     * Average feedback rating per (trigger, masking_pct) combination.
     * Only includes combinations with at least 5 data points to avoid noisy results.
     * This drives the scatter/line chart on Screen 3.
     */
    @Query("""
        SELECT
          te.trigger_id       as triggerId,
          te.masking_pct_applied as maskingPctApplied,
          AVG(ef.rating)      as avgRating,
          COUNT(*)            as totalEvents
        FROM trigger_events te
        JOIN event_feedback ef ON ef.event_id = te.event_id
        GROUP BY te.trigger_id, te.masking_pct_applied
        HAVING COUNT(*) >= 5
        ORDER BY te.trigger_id, te.masking_pct_applied
    """)
    fun getEffectivenessData(): Flow<List<EffectivenessPointDto>>

    /**
     * Current masking percentage for every active trigger.
     * Used to draw a "you are here" marker on the effectiveness chart
     * and to compute the Nudge suggestion.
     */
    @Query("""
        SELECT trigger_id as triggerId, masking_percentage as maskingPercentage
        FROM user_trigger_config
        WHERE is_active = 1
    """)
    fun getCurrentMasking(): Flow<List<CurrentMaskingDto>>
}
