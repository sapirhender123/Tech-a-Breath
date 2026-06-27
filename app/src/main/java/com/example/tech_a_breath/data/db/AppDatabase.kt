package com.example.tech_a_breath.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tech_a_breath.data.db.dao.*
import com.example.tech_a_breath.data.db.entity.*

@Database(
    entities = [
        TriggerEntity::class,
        UserTriggerConfigEntity::class,
        TriggerEventEntity::class,
        TriggerConfigHistoryEntity::class,
        EventFeedbackEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun triggerDao(): TriggerDao
    abstract fun weeklyDashboardDao(): WeeklyDashboardDao
    abstract fun monthlyDashboardDao(): MonthlyDashboardDao
    abstract fun effectivenessDao(): EffectivenessDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance.
         *
         * [passphrase] is the SQLCipher encryption key.
         * Retrieve it from Android Keystore in production — never hardcode it.
         *
         * To develop without encryption (Stage 1), replace the factory block
         * with a plain Room.databaseBuilder call and remove the SupportFactory.
         */
        fun getInstance(context: Context, passphrase: ByteArray): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Load SQLCipher native libs before opening the encrypted DB
                net.sqlcipher.database.SQLiteDatabase.loadLibs(context)
                val factory = net.sqlcipher.database.SupportFactory(passphrase)
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tech_a_breath.db"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Unencrypted instance — use only during development / unit tests.
         */
        fun getInstanceUnencrypted(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tech_a_breath_dev.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
