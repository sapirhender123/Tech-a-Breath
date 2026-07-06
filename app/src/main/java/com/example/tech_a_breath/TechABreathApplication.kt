package com.example.tech_a_breath

import android.app.Application
import com.example.tech_a_breath.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TechABreathApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    override fun onCreate() {
        super.onCreate()
        TriggerManager.init(database, applicationScope)
        HeadphoneManager.init(this)
    }
}
