package com.example.tech_a_breath.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EffectivenessDao {

    @Query("""
        SELECT 
          e.trigger_id as triggerId,
          e.masking_pct_applied as maskingPctApplied,
          AVG(f.rating) as avgRating,
          COUNT(e.event_id) as totalEvents
        FROM trigger_events e
        JOIN event_feedback f ON e.event_id = f.event_id
        GROUP BY triggerId, maskingPctApplied
    """)
    fun getEffectivenessData(): Flow<List<EffectivenessPointDto>>

    @Query("""
        SELECT 
          trigger_id as triggerId,
          masking_percentage as maskingPercentage
        FROM user_trigger_config
        WHERE is_active = 1
    """)
    fun getCurrentMasking(): Flow<List<CurrentMaskingDto>>
}
