package com.example.tech_a_breath.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trigger_events",
    foreignKeys = [
        ForeignKey(
            entity = TriggerEntity::class,
            parentColumns = ["trigger_id"],
            childColumns = ["trigger_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TriggerEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "event_id")
    val eventId: Long = 0,

    @ColumnInfo(name = "trigger_id")
    val triggerId: Int,

    @ColumnInfo(name = "detected_at")
    val detectedAt: Long, // Unix Epoch time

    @ColumnInfo(name = "masking_pct_applied")
    val maskingPctApplied: Int,

    @ColumnInfo(name = "response_type_used")
    val responseTypeUsed: String,

    @ColumnInfo(name = "latency_ms")
    val latencyMs: Long,

    @ColumnInfo(name = "event_duration_ms")
    val eventDurationMs: Long,

    @ColumnInfo(name = "ended_at")
    val endedAt: Long? // Nullable until masking is stopped
)
