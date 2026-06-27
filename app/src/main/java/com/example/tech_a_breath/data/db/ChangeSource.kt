package com.example.tech_a_breath.data.db

enum class ChangeSource(val dbValue: String) {
    USER_MANUAL("user_manual"),
    APP_NUDGE("app_nudge");

    companion object {
        fun fromDbValue(value: String): ChangeSource =
            values().firstOrNull { it.dbValue == value } ?: USER_MANUAL
    }
}
