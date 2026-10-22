package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookNumber: Int,
    val unitNumber: Int,
    val word: String,
    val phonetic: String,
    val englishDefinition: String = "",
    val englishExample: String = "",
    val definition: String = "",
    val uzbekTranslation: String = "",
    val uzbekDefinition: String = "",
    val exampleSentence: String = "",
    val exampleSentenceEn: String = "",
    val exampleSentenceUz: String = "",
    val synonyms: String = "",
    val antonyms: String = "",
    val isBookmarked: Boolean = false,
    val isLearned: Boolean = false,
    val learnedTimestamp: Long = 0L
)

@Entity(tableName = "unit_progress")
data class UnitProgressEntity(
    @PrimaryKey
    val unitId: String, // e.g. "1_1" for bookNumber_unitNumber
    val bookNumber: Int,
    val unitNumber: Int,
    val unitTitle: String = "",
    val isUnlocked: Boolean = false,
    val isPassed: Boolean = false,
    val score: Int = 0,
    val masteryLevel: Float = 0f
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userName: String = "Learner",
    val bookNumber: Int,
    val unitNumber: Int,
    val score: Int,
    val totalQuestions: Int = 10,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_cache")
data class QuizCacheEntity(
    @PrimaryKey
    val word: String,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val explanation: String = "",
    val cachedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

