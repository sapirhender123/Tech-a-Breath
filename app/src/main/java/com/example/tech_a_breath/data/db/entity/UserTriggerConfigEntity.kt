package com.example.tech_a_breath.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tech_a_breath.data.db.ResponseType

/**
 * The user's masking strategy per trigger — read at runtime to decide how to respond.
 * Every change here must also write a row to [TriggerConfigHistoryEntity].
 *
 * updated_at stored as Unix epoch millis (Long) to match all other timestamp columns.
 */
@Entity(
    tableName = "user_trigger_config",
    foreignKeys = [
        ForeignKey(
            entity = TriggerEntity::class,
            parentColumns = ["trigger_id"],
            childColumns = ["trigger_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trigger_id")]
)
data class UserTriggerConfigEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "config_id")
    val configId: Long = 0,

    @ColumnInfo(name = "trigger_id")
    val triggerId: Int,

    /** 0 = inactive, 1 = active */
    @ColumnInfo(name = "is_active")
    val isActive: Int,

    /** 1–5 */
    @ColumnInfo(name = "sensitivity_level")
    val sensitivityLevel: Int,

    /** 0–100 */
    @ColumnInfo(name = "masking_percentage")
    val maskingPercentage: Int,

    @ColumnInfo(name = "response_type")
    val responseType: ResponseType,

    /** Unix epoch millis */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
