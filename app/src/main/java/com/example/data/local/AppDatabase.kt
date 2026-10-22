package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WordEntity::class,
        UnitProgressEntity::class,
        LeaderboardEntity::class,
        QuizCacheEntity::class,
        SearchHistoryEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun unitProgressDao(): UnitProgressDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun quizCacheDao(): QuizCacheDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vocabulary_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
