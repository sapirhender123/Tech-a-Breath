package com.example.tech_a_breath.data.db

import android.content.Context
import com.example.tech_a_breath.data.db.entity.TriggerEntity
import com.example.tech_a_breath.data.repository.DashboardRepository
import com.example.tech_a_breath.data.repository.DashboardRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Simple manual DI provider — no Hilt/Dagger required.
 * The repository is created lazily the first time it is requested.
 * Trigger seeding runs on an IO coroutine so it never blocks the main thread.
 */
object DatabaseProvider {

    @Volatile
    private var repository: DashboardRepository? = null

    fun getRepository(context: Context): DashboardRepository {
        return repository ?: synchronized(this) {
            repository ?: buildRepository(context).also { repository = it }
        }
    }

    private fun buildRepository(context: Context): DashboardRepository {
        val db = AppDatabase.getInstanceUnencrypted(context)

        val repo = DashboardRepositoryImpl(
            triggerDao       = db.triggerDao(),
            weeklyDao        = db.weeklyDashboardDao(),
            monthlyDao       = db.monthlyDashboardDao(),
            effectivenessDao = db.effectivenessDao()
        )

        // Seed reference + mock data on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            db.triggerDao().insertAll(
                listOf(
                    TriggerEntity(triggerId = 1, modelLabel = "motorcycle"),
                    TriggerEntity(triggerId = 2, modelLabel = "dog_bark"),
                    TriggerEntity(triggerId = 3, modelLabel = "siren"),
                    TriggerEntity(triggerId = 4, modelLabel = "firework")
                )
            )
            MockDataSeeder.seedIfEmpty(db)
        }

        return repo
    }
}
