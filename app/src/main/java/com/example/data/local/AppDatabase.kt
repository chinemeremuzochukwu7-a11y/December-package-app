package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteWishEntity::class,
        SavedCardEntity::class,
        AiGeneratedWishEntity::class,
        BirthdayEntity::class,
        ExpenseEntity::class,
        MealEntity::class,
        AlarmEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteWishDao(): FavoriteWishDao
    abstract fun savedCardDao(): SavedCardDao
    abstract fun aiGeneratedWishDao(): AiGeneratedWishDao
    abstract fun birthdayDao(): BirthdayDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun mealDao(): MealDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "holiday_wishes.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
