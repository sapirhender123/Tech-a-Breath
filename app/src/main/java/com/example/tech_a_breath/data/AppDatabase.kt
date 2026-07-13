package com.example.tech_a_breath.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TriggerEntity::class,
        UserTriggerConfigEntity::class,
        TriggerConfigHistoryEntity::class,
        TriggerEventEntity::class,
        EventFeedbackEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun triggerDao(): TriggerDao
    abstract fun userTriggerConfigDao(): UserTriggerConfigDao
    abstract fun triggerConfigHistoryDao(): TriggerConfigHistoryDao
    abstract fun triggerEventDao(): TriggerEventDao
    abstract fun eventFeedbackDao(): EventFeedbackDao

    // Dashboard DAOs
    abstract fun weeklyDashboardDao(): WeeklyDashboardDao
    abstract fun monthlyDashboardDao(): MonthlyDashboardDao
    abstract fun effectivenessDao(): EffectivenessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tech_a_breath_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Unencrypted instance for development.
         */
        fun getInstanceUnencrypted(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tech_a_breath_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(database: AppDatabase) {
                val triggerDao = database.triggerDao()

                val triggers = listOf(
                    TriggerEntity(id = 1, modelLabel = "motorcycle"),
                    TriggerEntity(id = 2, modelLabel = "dog_bark"),
                    TriggerEntity(id = 3, modelLabel = "siren"),
                    TriggerEntity(id = 4, modelLabel = "firework")
                )
                triggerDao.insertAll(triggers)
            }
        }
    }
}
