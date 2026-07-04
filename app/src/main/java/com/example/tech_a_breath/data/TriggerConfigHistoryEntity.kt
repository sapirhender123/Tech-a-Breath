package com.example.tech_a_breath.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trigger_config_history",
    foreignKeys = [
        ForeignKey(
            entity = TriggerEntity::class,
            parentColumns = ["trigger_id"],
            childColumns = ["trigger_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TriggerConfigHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "history_id")
    val historyId: Int = 0,

    @ColumnInfo(name = "trigger_id")
    val triggerId: Int,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "sensitivity_level")
    val sensitivityLevel: Int,

    @ColumnInfo(name = "masking_percentage")
    val maskingPercentage: Int,

    @ColumnInfo(name = "response_type")
    val responseType: String,

    @ColumnInfo(name = "change_source")
    val changeSource: String, // "user_manual" or "app_nudge"

    @ColumnInfo(name = "changed_at")
    val changedAt: Long // Unix Epoch time
)
