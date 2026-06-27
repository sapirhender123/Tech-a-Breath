package com.example.tech_a_breath.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Reference table — seeded at install time, never changed by the user.
 * Maps the AI model's string label to an internal integer ID used in every JOIN.
 */
@Entity(
    tableName = "triggers",
    indices = [Index(value = ["model_label"], unique = true)]
)
data class TriggerEntity(

    @PrimaryKey
    @ColumnInfo(name = "trigger_id")
    val triggerId: Int,

    /** The label returned by the TFLite classifier, e.g. "dog_bark" */
    @ColumnInfo(name = "model_label")
    val modelLabel: String
)
