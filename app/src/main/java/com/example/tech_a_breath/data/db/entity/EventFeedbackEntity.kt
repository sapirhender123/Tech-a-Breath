package com.example.tech_a_breath.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * End-of-day feedback submitted by the user for a detected event.
 * UNIQUE on event_id enforced at DB level — one feedback per event.
 */
@Entity(
    tableName = "event_feedback",
    foreignKeys = [
        ForeignKey(
            entity = TriggerEventEntity::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["event_id"], unique = true),
        Index("submitted_at")
    ]
)
data class EventFeedbackEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "feedback_id")
    val feedbackId: Long = 0,

    @ColumnInfo(name = "event_id")
    val eventId: Long,

    /** 1–5 */
    @ColumnInfo(name = "rating")
    val rating: Int,

    /** Optional free-text note from the user */
    @ColumnInfo(name = "note")
    val note: String?,

    /** Unix epoch millis */
    @ColumnInfo(name = "submitted_at")
    val submittedAt: Long
)
