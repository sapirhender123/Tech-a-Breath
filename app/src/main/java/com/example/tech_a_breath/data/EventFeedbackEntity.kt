package com.example.tech_a_breath.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_feedback",
    indices = [Index(value = ["event_id"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = TriggerEventEntity::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventFeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "feedback_id")
    val feedbackId: Int = 0,

    @ColumnInfo(name = "event_id")
    val eventId: Long,

    @ColumnInfo(name = "rating")
    val rating: Int, // 1-5

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "submitted_at")
    val submittedAt: Long
)
