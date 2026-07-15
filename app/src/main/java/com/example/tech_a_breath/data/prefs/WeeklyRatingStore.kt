package com.example.tech_a_breath.data.prefs

import android.content.Context
import java.util.Calendar

/**
 * Persists the user's weekly mood rating (1–4) in SharedPreferences.
 * Key is scoped to the ISO week number so it resets automatically each week.
 *
 * NOT stored in event_feedback — that table requires a valid event_id FK
 * and is designed for per-event monitoring feedback.
 */
class WeeklyRatingStore(context: Context) {

    private val prefs = context.getSharedPreferences("friendly_dashboard", Context.MODE_PRIVATE)

    private val weekKey: String
        get() {
            val week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
            val year = Calendar.getInstance().get(Calendar.YEAR)
            return "weekly_rating_${year}_$week"
        }

    fun saveRating(rating: Int) {
        prefs.edit().putInt(weekKey, rating).apply()
    }

    fun getRating(): Int? {
        val value = prefs.getInt(weekKey, -1)
        return if (value == -1) null else value
    }
}
