package com.example.tech_a_breath.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tech_a_breath.data.db.ResponseType

/**
 * One row per detected trigger event.
 * [maskingPctApplied] and [responseTypeUsed] are snapshots — they record what was
 * actually used, not the current config, so historical data stays accurate.
 */
@Entity(
    tableName = "trigger_events",
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
        Index("detected_at")
    ]
)
data class TriggerEventEntity(

    @PrimaryKey
    @ColumnInfo(name = "event_id")
    val eventId: Long,

    @ColumnInfo(name = "trigger_id")
    val triggerId: Int,

    /** Unix epoch millis */
    @ColumnInfo(name = "detected_at")
    val detectedAt: Long,

    /** Snapshot of masking % at the time of the event (0–100) */
    @ColumnInfo(name = "masking_pct_applied")
    val maskingPctApplied: Int,

    /** Snapshot of response type at the time of the event */
    @ColumnInfo(name = "response_type_used")
    val responseTypeUsed: ResponseType,

    /** Millis from detection to masking start — must be positive */
    @ColumnInfo(name = "latency_ms")
    val latencyMs: Long,

    /** Total duration of the masking session in millis — nullable until session ends */
    @ColumnInfo(name = "event_duration_ms")
    val eventDurationMs: Long?,

    /** Unix epoch millis — null until masking stops */
    @ColumnInfo(name = "ended_at")
    val endedAt: Long?
)
