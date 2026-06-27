package com.example.tech_a_breath.data.db

enum class ResponseType(val dbValue: String) {
    MUSIC("music"),
    WHITE_NOISE("white_noise"),
    BREATHING("breathing"),
    VIBRATION("vibration");

    companion object {
        fun fromDbValue(value: String): ResponseType =
            values().firstOrNull { it.dbValue == value } ?: WHITE_NOISE
    }
}
