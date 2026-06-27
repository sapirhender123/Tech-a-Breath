package com.example.tech_a_breath.data.db

import com.example.tech_a_breath.data.db.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Inserts realistic demo data across all 5 tables.
 * Safe to call multiple times — checks if data already exists first.
 */
object MockDataSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) = withContext(Dispatchers.IO) {
        // Only seed once
        if (db.weeklyDashboardDao().getWeeklyTotalEventsOnce() > 0) return@withContext
        seed(db)
    }

    private suspend fun seed(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val day = TimeUnit.DAYS.toMillis(1)
        val hour = TimeUnit.HOURS.toMillis(1)

        // ── user_trigger_config ───────────────────────────────────────────────
        db.triggerDao().insertConfigs(
            listOf(
                UserTriggerConfigEntity(configId = 1, triggerId = 1, isActive = 1, sensitivityLevel = 3, maskingPercentage = 80,  responseType = ResponseType.WHITE_NOISE, updatedAt = now - 2 * day),
                UserTriggerConfigEntity(configId = 2, triggerId = 2, isActive = 1, sensitivityLevel = 5, maskingPercentage = 100, responseType = ResponseType.WHITE_NOISE, updatedAt = now - 5 * day),
                UserTriggerConfigEntity(configId = 3, triggerId = 3, isActive = 1, sensitivityLevel = 5, maskingPercentage = 100, responseType = ResponseType.MUSIC,       updatedAt = now - 1 * day),
                UserTriggerConfigEntity(configId = 4, triggerId = 4, isActive = 0, sensitivityLevel = 2, maskingPercentage = 60,  responseType = ResponseType.BREATHING,    updatedAt = now - 10 * day)
            )
        )

        // ── trigger_events + event_feedback ───────────────────────────────────
        // Generate ~40 events spread over the last 30 days
        val eventRows    = mutableListOf<TriggerEventEntity>()
        val feedbackRows = mutableListOf<EventFeedbackEntity>()

        // Distribution: siren(3) most frequent, dog_bark(2) medium, motorcycle(1) occasional, firework(4) rare
        val triggerWeights = listOf(
            1 to 6,   // motorcycle  — weight 6
            2 to 10,  // dog_bark    — weight 10
            3 to 14,  // siren       — weight 14
            4 to 3    // firework    — weight 3
        )
        val triggerPool = triggerWeights.flatMap { (id, w) -> List(w) { id } }

        val responseByTrigger = mapOf(
            1 to ResponseType.WHITE_NOISE,
            2 to ResponseType.WHITE_NOISE,
            3 to ResponseType.MUSIC,
            4 to ResponseType.BREATHING
        )
        val maskingByTrigger = mapOf(1 to 80, 2 to 100, 3 to 100, 4 to 60)

        // Spread events across last 30 days with realistic hour clusters (8-10, 17-20, 22-23)
        val peakHours = listOf(8, 9, 10, 17, 18, 19, 20, 22, 23)
        var eventId = 1L

        repeat(45) { i ->
            val daysAgo     = Random.nextInt(0, 30)
            val peakHour    = peakHours[Random.nextInt(peakHours.size)]
            val minuteJitter= Random.nextLong(0, 59) * 60_000L
            val detectedAt  = now - daysAgo * day - (24 - peakHour) * hour + minuteJitter
            val durationMs  = when (Random.nextInt(3)) {
                0    -> Random.nextLong(3_000, 9_999)    // short  < 10s
                1    -> Random.nextLong(10_000, 30_000)  // medium 10–30s
                else -> Random.nextLong(30_001, 60_000)  // long   > 30s
            }
            val triggerId   = triggerPool[Random.nextInt(triggerPool.size)]
            val masking     = maskingByTrigger[triggerId] ?: 80
            // Vary masking slightly to get multiple data points for effectiveness chart
            val maskingApplied = when (Random.nextInt(4)) {
                0    -> (masking - 20).coerceAtLeast(20)
                1    -> (masking - 10).coerceAtLeast(20)
                else -> masking
            }

            eventRows.add(
                TriggerEventEntity(
                    eventId          = eventId,
                    triggerId        = triggerId,
                    detectedAt       = detectedAt,
                    maskingPctApplied = maskingApplied,
                    responseTypeUsed = responseByTrigger[triggerId] ?: ResponseType.WHITE_NOISE,
                    latencyMs        = Random.nextLong(200, 500),
                    eventDurationMs  = durationMs,
                    endedAt          = detectedAt + durationMs
                )
            )

            // ~75% of events get feedback
            if (Random.nextFloat() > 0.25f) {
                // Higher masking → higher rating (with some noise) — makes effectiveness chart interesting
                val baseRating = when {
                    maskingApplied >= 90 -> 4
                    maskingApplied >= 70 -> 3
                    else                 -> 2
                }
                val rating = (baseRating + Random.nextInt(-1, 2)).coerceIn(1, 5)
                feedbackRows.add(
                    EventFeedbackEntity(
                        eventId     = eventId,
                        rating      = rating,
                        note        = null,
                        submittedAt = detectedAt + durationMs + Random.nextLong(hour, 8 * hour)
                    )
                )
            }

            eventId++
        }

        db.weeklyDashboardDao().insertAllEvents(eventRows)
        db.weeklyDashboardDao().insertAllFeedback(feedbackRows)

        // ── trigger_config_history ────────────────────────────────────────────
        db.triggerDao().insertConfigHistory(
            listOf(
                TriggerConfigHistoryEntity(triggerId = 3, sensitivityLevel = 5, maskingPercentage = 60,  changeSource = ChangeSource.USER_MANUAL, changedAt = now - 28 * day),
                TriggerConfigHistoryEntity(triggerId = 3, sensitivityLevel = 5, maskingPercentage = 80,  changeSource = ChangeSource.APP_NUDGE,   changedAt = now - 21 * day),
                TriggerConfigHistoryEntity(triggerId = 3, sensitivityLevel = 5, maskingPercentage = 100, changeSource = ChangeSource.USER_MANUAL, changedAt = now - 14 * day),
                TriggerConfigHistoryEntity(triggerId = 2, sensitivityLevel = 4, maskingPercentage = 80,  changeSource = ChangeSource.USER_MANUAL, changedAt = now - 20 * day),
                TriggerConfigHistoryEntity(triggerId = 2, sensitivityLevel = 5, maskingPercentage = 100, changeSource = ChangeSource.APP_NUDGE,   changedAt = now - 10 * day),
                TriggerConfigHistoryEntity(triggerId = 1, sensitivityLevel = 3, maskingPercentage = 60,  changeSource = ChangeSource.USER_MANUAL, changedAt = now - 15 * day),
                TriggerConfigHistoryEntity(triggerId = 1, sensitivityLevel = 3, maskingPercentage = 80,  changeSource = ChangeSource.APP_NUDGE,   changedAt = now - 5  * day)
            )
        )
    }
}
