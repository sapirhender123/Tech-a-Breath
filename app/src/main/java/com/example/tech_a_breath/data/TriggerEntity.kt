package com.example.tech_a_breath.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "triggers",
    indices = [Index(value = ["model_label"], unique = true)]
)
data class TriggerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trigger_id")
    val id: Int = 0,
    
    @ColumnInfo(name = "model_label")
    val modelLabel: String
)
