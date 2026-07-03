package com.example.tech_a_breath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TriggerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(triggers: List<TriggerEntity>)

    @Query("SELECT * FROM triggers")
    suspend fun getAllTriggers(): List<TriggerEntity>
}
