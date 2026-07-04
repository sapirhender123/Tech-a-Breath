package com.example.tech_a_breath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TriggerConfigHistoryDao {
    @Insert
    suspend fun insert(history: TriggerConfigHistoryEntity)

    @Query("SELECT * FROM trigger_config_history WHERE trigger_id = :triggerId ORDER BY changed_at DESC")
    suspend fun getHistoryForTrigger(triggerId: Int): List<TriggerConfigHistoryEntity>

    @Query("SELECT * FROM trigger_config_history ORDER BY changed_at DESC")
    suspend fun getAllHistory(): List<TriggerConfigHistoryEntity>
}
