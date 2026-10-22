package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE bookNumber = :book AND unitNumber = :unit ORDER BY id ASC")
    fun getWordsByUnit(book: Int, unit: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE bookNumber = :book ORDER BY unitNumber, id ASC")
    fun getWordsByBook(book: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE bookNumber = :book ORDER BY unitNumber, id ASC")
    suspend fun getWordsByBookList(book: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%' OR englishDefinition LIKE '%' || :query || '%' OR uzbekTranslation LIKE '%' || :query || '%' ORDER BY bookNumber, unitNumber, id ASC")
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :wordId LIMIT 1")
    suspend fun getWordById(wordId: Long): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("UPDATE words SET isBookmarked = :isBookmarked WHERE id = :wordId")
    suspend fun updateBookmarkStatus(wordId: Long, isBookmarked: Boolean)

    @Query("UPDATE words SET isLearned = :isLearned, learnedTimestamp = :timestamp WHERE id = :wordId")
    suspend fun updateLearnedStatus(wordId: Long, isLearned: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM words WHERE isLearned = 1 ORDER BY bookNumber, unitNumber, id ASC")
    fun getLearnedWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 1")
    fun getLearnedCount(): Flow<Int>

    @Query("SELECT * FROM words WHERE isBookmarked = 1 ORDER BY bookNumber, unitNumber, id ASC")
    fun getBookmarkedWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE isBookmarked = 1")
    fun getBookmarkedCount(): Flow<Int>

    @Query("SELECT * FROM words WHERE synonyms != '' OR antonyms != '' ORDER BY bookNumber, unitNumber, id ASC")
    fun getWordsWithSynonymsOrAntonyms(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words ORDER BY bookNumber, unitNumber, id ASC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words ORDER BY bookNumber, unitNumber, id ASC")
    suspend fun getAllWordsList(): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("SELECT * FROM words WHERE isLearned = 1 AND learnedTimestamp > 0 AND learnedTimestamp <= :cutoffTimestamp")
    suspend fun getWordsForSpacedReview(cutoffTimestamp: Long): List<WordEntity>
}

@Dao
interface UnitProgressDao {
    @Query("SELECT * FROM unit_progress ORDER BY bookNumber, unitNumber ASC")
    fun getAllProgress(): Flow<List<UnitProgressEntity>>

    @Query("SELECT * FROM unit_progress WHERE bookNumber = :book ORDER BY unitNumber ASC")
    fun getProgressForBook(book: Int): Flow<List<UnitProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProgress(progress: UnitProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitial(progressList: List<UnitProgressEntity>)

    @Query("UPDATE unit_progress SET isPassed = 1, isUnlocked = 1, score = :score, masteryLevel = :score WHERE bookNumber = :book AND unitNumber = :unit")
    suspend fun markUnitPassed(book: Int, unit: Int, score: Int)

    @Query("UPDATE unit_progress SET isUnlocked = 1 WHERE bookNumber = :book AND unitNumber = :unit")
    suspend fun unlockUnit(book: Int, unit: Int)

    @Query("UPDATE unit_progress SET isUnlocked = 1")
    suspend fun unlockAllUnits()

    @Query("UPDATE unit_progress SET isUnlocked = 1 WHERE bookNumber = :book")
    suspend fun unlockAllUnitsForBook(book: Int)

    @Query("UPDATE unit_progress SET isUnlocked = 0 WHERE bookNumber = :book")
    suspend fun lockAllUnitsForBook(book: Int)

    @Query("SELECT COUNT(*) FROM unit_progress")
    suspend fun getCount(): Int

    @Query("DELETE FROM unit_progress")
    suspend fun deleteAllProgress()

    @Query("SELECT * FROM unit_progress ORDER BY bookNumber, unitNumber ASC")
    suspend fun getAllProgressList(): List<UnitProgressEntity>
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard ORDER BY score DESC, timestamp DESC LIMIT :limit")
    fun getTopScores(limit: Int = 10): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard ORDER BY score DESC, timestamp DESC LIMIT 5")
    fun getTop5Scores(): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<LeaderboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: LeaderboardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScores(scores: List<LeaderboardEntity>)

    @Query("SELECT COUNT(*) FROM leaderboard")
    suspend fun getCount(): Int
}

@Dao
interface QuizCacheDao {
    @Query("SELECT * FROM quiz_cache WHERE word = :word LIMIT 1")
    suspend fun getCachedQuiz(word: String): QuizCacheEntity?

    @Query("SELECT * FROM quiz_cache ORDER BY cachedTimestamp DESC LIMIT :limit")
    suspend fun getRecentCachedQuizzes(limit: Int = 10): List<QuizCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizCacheEntity)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearchByQuery(query: String)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

