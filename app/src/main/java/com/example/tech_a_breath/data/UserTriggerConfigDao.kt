package com.example.tech_a_breath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserTriggerConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: UserTriggerConfigEntity): Long

    @Query("SELECT * FROM user_trigger_config")
    suspend fun getAllConfigs(): List<UserTriggerConfigEntity>

    @Query("SELECT * FROM user_trigger_config WHERE trigger_id = :triggerId")
    suspend fun getConfigForTrigger(triggerId: Int): UserTriggerConfigEntity?
}
