package com.example.tech_a_breath.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tech_a_breath.data.db.entity.TriggerConfigHistoryEntity
import com.example.tech_a_breath.data.db.entity.TriggerEntity
import com.example.tech_a_breath.data.db.entity.UserTriggerConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(triggers: List<TriggerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<UserTriggerConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigHistory(history: List<TriggerConfigHistoryEntity>)

    @Query("SELECT * FROM triggers")
    fun getAllTriggers(): Flow<List<TriggerEntity>>

    @Query("SELECT * FROM triggers WHERE trigger_id = :id")
    suspend fun getById(id: Int): TriggerEntity?
}
