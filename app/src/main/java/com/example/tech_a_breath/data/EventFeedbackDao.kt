package com.example.tech_a_breath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feedback: EventFeedbackEntity): Long

    @Query("SELECT * FROM event_feedback WHERE event_id = :eventId")
    suspend fun getFeedbackForEvent(eventId: Long): EventFeedbackEntity?

    @Query("SELECT * FROM event_feedback ORDER BY submitted_at DESC")
    suspend fun getAllFeedback(): List<EventFeedbackEntity>
}
