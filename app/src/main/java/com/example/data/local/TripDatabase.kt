package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TripEntity::class,
        TripDayEntity::class,
        ActivityEntity::class,
        TravelerEntity::class,
        ExpenseEntity::class,
        BookingEntity::class,
        PackingItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: TripDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trips ADD COLUMN startingCity TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE activities ADD COLUMN imageUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): TripDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TripDatabase::class.java,
                    "tripbudget.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
