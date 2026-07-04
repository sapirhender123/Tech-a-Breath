package com.example.tech_a_breath.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_trigger_config",
    indices = [androidx.room.Index(value = ["trigger_id"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = TriggerEntity::class,
            parentColumns = ["trigger_id"],
            childColumns = ["trigger_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserTriggerConfigEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "config_id")
    val configId: Int = 0,

    @ColumnInfo(name = "trigger_id")
    val triggerId: Int,

    @ColumnInfo(name = "is_active", defaultValue = "0")
    val isActive: Boolean,

    @ColumnInfo(name = "sensitivity_level")
    val sensitivityLevel: Int,

    @ColumnInfo(name = "masking_percentage")
    val maskingPercentage: Int,

    @ColumnInfo(name = "response_type", defaultValue = "white_noise")
    val responseType: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
