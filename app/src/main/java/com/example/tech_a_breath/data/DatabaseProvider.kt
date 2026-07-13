package com.example.tech_a_breath.data

import android.content.Context
import com.example.tech_a_breath.data.repository.DashboardRepository
import com.example.tech_a_breath.data.repository.DashboardRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Simple manual DI provider.
 * The repository is created lazily the first time it is requested.
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
        val db = AppDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))

        return DashboardRepositoryImpl(
            triggerDao       = db.triggerDao(),
            weeklyDao        = db.weeklyDashboardDao(),
            monthlyDao       = db.monthlyDashboardDao(),
            effectivenessDao = db.effectivenessDao()
        )
    }
}
