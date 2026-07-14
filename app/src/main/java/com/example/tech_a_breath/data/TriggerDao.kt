package com.example.tech_a_breath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(triggers: List<TriggerEntity>)

    @Query("SELECT * FROM triggers")
    suspend fun getAllTriggers(): List<TriggerEntity>

    @Query("SELECT * FROM triggers")
    fun getAllTriggersFlow(): Flow<List<TriggerEntity>>

    @Query("UPDATE user_trigger_config SET masking_percentage = :pct WHERE trigger_id = :triggerId")
    suspend fun updateMaskingPercentage(triggerId: Int, pct: Int)
}
