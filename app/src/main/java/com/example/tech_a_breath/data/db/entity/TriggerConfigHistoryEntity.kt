package com.example.tech_a_breath.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tech_a_breath.data.db.ChangeSource

/**
 * Full-snapshot history of every config change.
 * Written whenever [UserTriggerConfigEntity] is updated.
 * [changeSource] distinguishes user-initiated vs app-nudge changes.
 */
@Entity(
    tableName = "trigger_config_history",
    foreignKeys = [
        ForeignKey(
            entity = TriggerEntity::class,
            parentColumns = ["trigger_id"],
            childColumns = ["trigger_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("trigger_id"),
        Index("changed_at")
    ]
)
data class TriggerConfigHistoryEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "history_id")
    val historyId: Long = 0,

    @ColumnInfo(name = "trigger_id")
    val triggerId: Int,

    /** 1–5 */
    @ColumnInfo(name = "sensitivity_level")
    val sensitivityLevel: Int,

    /** 0–100 */
    @ColumnInfo(name = "masking_percentage")
    val maskingPercentage: Int,

    @ColumnInfo(name = "change_source")
    val changeSource: ChangeSource,

    /** Unix epoch millis */
    @ColumnInfo(name = "changed_at")
    val changedAt: Long
)
