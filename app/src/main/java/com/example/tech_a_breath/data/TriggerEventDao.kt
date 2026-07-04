package com.example.tech_a_breath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TriggerEventDao {
    @Insert
    suspend fun insert(event: TriggerEventEntity): Long

    @Update
    suspend fun update(event: TriggerEventEntity)

    @Query("SELECT * FROM trigger_events ORDER BY detected_at DESC")
    suspend fun getAllEvents(): List<TriggerEventEntity>

    @Query("SELECT * FROM trigger_events WHERE trigger_id = :triggerId ORDER BY detected_at DESC")
    suspend fun getEventsForTrigger(triggerId: Int): List<TriggerEventEntity>

    @Query("SELECT * FROM trigger_events WHERE event_id = :eventId")
    suspend fun getEventById(eventId: Long): TriggerEventEntity?
}
