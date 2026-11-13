package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.LeaderboardEntity
import com.example.data.local.QuizCacheEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.UnitProgressEntity
import com.example.data.local.WordEntity
import com.example.util.GeminiHelper
import com.example.util.StreakManager
import com.example.util.UnitTitlesHelper
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class VocabularyRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val wordDao = database.wordDao()
    private val unitProgressDao = database.unitProgressDao()
    private val leaderboardDao = database.leaderboardDao()
    private val quizCacheDao = database.quizCacheDao()
    private val searchHistoryDao = database.searchHistoryDao()
    private val geminiHelper = GeminiHelper()

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        try {
            // Seed Unit Progress for all 6 books (30 units each = 180 units)
            val currentCount = unitProgressDao.getCount()
            if (currentCount < 180) {
                val initialUnits = mutableListOf<UnitProgressEntity>()
                for (book in 1..6) {
                    for (unit in 1..30) {
                        val title = UnitTitlesHelper.getTitle(book, unit)
                        initialUnits.add(
                            UnitProgressEntity(
                                unitId = "${book}_$unit",
                                bookNumber = book,
                                unitNumber = unit,
                                unitTitle = title,
                                isUnlocked = true, // All units unlocked by default for study
                                isPassed = false,
                                score = 0,
                                masteryLevel = 0f
                            )
                        )
                    }
                }
                unitProgressDao.insertInitial(initialUnits)
            } else {
                // Ensure all units are unlocked by default
                unitProgressDao.unlockAllUnits()
            }

            // Seed initial top 5 leaderboard entries if table is empty
            if (leaderboardDao.getCount() == 0) {
                val initialLeaderboard = listOf(
                    LeaderboardEntity(userName = "Alisher N.", bookNumber = 1, unitNumber = 15, score = 10, totalQuestions = 10, timestamp = System.currentTimeMillis() - 3600000 * 2),
                    LeaderboardEntity(userName = "Dilnoza K.", bookNumber = 2, unitNumber = 20, score = 10, totalQuestions = 10, timestamp = System.currentTimeMillis() - 3600000 * 5),
                    LeaderboardEntity(userName = "Sardor B.", bookNumber = 1, unitNumber = 30, score = 9, totalQuestions = 10, timestamp = System.currentTimeMillis() - 3600000 * 12),
                    LeaderboardEntity(userName = "Madina T.", bookNumber = 3, unitNumber = 5, score = 9, totalQuestions = 10, timestamp = System.currentTimeMillis() - 3600000 * 24),
                    LeaderboardEntity(userName = "Jasur M.", bookNumber = 1, unitNumber = 10, score = 8, totalQuestions = 10, timestamp = System.currentTimeMillis() - 3600000 * 48)
                )
                leaderboardDao.insertScores(initialLeaderboard)
            }

            // Seed missing Words or backfill missing partOfSpeech from assets
            val needsSeed = wordDao.getWordCount() < 3600
            val needsPosBackfill = wordDao.getWordsMissingPartOfSpeechCount() > 0
            if (needsSeed || needsPosBackfill) {
                val existingIds = wordDao.getAllWordIds().toHashSet()
                val jsonString = context.assets.open("words_data.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val wordsList = mutableListOf<WordEntity>()
                val posBackfillPairs = mutableListOf<Pair<Long, String>>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optLong("id", i + 1L)
                    val partOfSpeech = obj.optString("partOfSpeech", "")
                    if (existingIds.contains(id)) {
                        if (needsPosBackfill && partOfSpeech.isNotBlank()) {
                            posBackfillPairs.add(id to partOfSpeech)
                        }
                        continue
                    }

                    val bookNum = obj.optInt("bookNumber", 1)
                    val unitNum = obj.optInt("unitNumber", 1)
                    val word = obj.optString("word", "")
                    val phonetic = obj.optString("phonetic", "")
                    val definition = obj.optString("definition", "")
                    val uzDef = obj.optString("uzbekDefinition", "")
                    val uzTr = obj.optString("uzbekTranslation", "")
                    val exSen = obj.optString("exampleSentence", "")
                    val exEn = obj.optString("exampleSentenceEn", exSen)
                    val exUz = obj.optString("exampleSentenceUz", "")

                    val synObj = obj.opt("synonyms")
                    val synStr = when (synObj) {
                        is JSONArray -> {
                            val list = mutableListOf<String>()
                            for (j in 0 until synObj.length()) list.add(synObj.getString(j))
                            list.joinToString(", ")
                        }
                        else -> synObj?.toString() ?: ""
                    }

                    val antObj = obj.opt("antonyms")
                    val antStr = when (antObj) {
                        is JSONArray -> {
                            val list = mutableListOf<String>()
                            for (j in 0 until antObj.length()) list.add(antObj.getString(j))
                            list.joinToString(", ")
                        }
                        else -> antObj?.toString() ?: ""
                    }

                    wordsList.add(
                        WordEntity(
                            id = id,
                            bookNumber = bookNum,
                            unitNumber = unitNum,
                            word = word,
                            phonetic = phonetic,
                            partOfSpeech = partOfSpeech,
                            englishDefinition = definition,
                            englishExample = exEn,
                            definition = definition,
                            uzbekTranslation = uzTr,
                            uzbekDefinition = uzDef,
                            exampleSentence = exSen,
                            exampleSentenceEn = exEn,
                            exampleSentenceUz = exUz,
                            synonyms = synStr,
                            antonyms = antStr,
                            isLearned = false,
                            isBookmarked = false,
                            learnedTimestamp = 0L
                        )
                    )
                }
                database.withTransaction {
                    if (wordsList.isNotEmpty()) {
                        wordDao.insertWords(wordsList)
                    }
                    if (posBackfillPairs.isNotEmpty()) {
                        for ((wordId, pos) in posBackfillPairs) {
                            wordDao.updatePartOfSpeech(wordId, pos)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("VocabRepo", "Error initializing vocabulary database", e)
        }
    }

    suspend fun resetAndRebuildDatabase() = withContext(Dispatchers.IO) {
        try {
            wordDao.deleteAllWords()
            unitProgressDao.deleteAllProgress()
            initializeDatabaseIfNeeded()
        } catch (e: Exception) {
            Log.e("VocabRepo", "Error resetting and rebuilding database", e)
        }
    }

    suspend fun ensureUnitsLoaded(bookNumber: Int) = withContext(Dispatchers.IO) {
        val existingUnits = unitProgressDao.getAllProgressList().filter { it.bookNumber == bookNumber }
        if (existingUnits.size < 30) {
            val toInsert = mutableListOf<UnitProgressEntity>()
            for (u in 1..30) {
                if (existingUnits.none { it.unitNumber == u }) {
                    toInsert.add(
                        UnitProgressEntity(
                            unitId = "${bookNumber}_$u",
                            bookNumber = bookNumber,
                            unitNumber = u,
                            unitTitle = UnitTitlesHelper.getTitle(bookNumber, u),
                            isUnlocked = true, // All units unlocked by default
                            isPassed = false,
                            score = 0
                        )
                    )
                }
            }
            if (toInsert.isNotEmpty()) {
                unitProgressDao.insertInitial(toInsert)
            }
        }
    }

    fun getWordsForUnit(bookNum: Int, unitNum: Int): Flow<List<WordEntity>> {
        return wordDao.getWordsByUnit(bookNum, unitNum)
    }

    fun getWordsForBook(bookNum: Int): Flow<List<WordEntity>> {
        return wordDao.getWordsByBook(bookNum)
    }

    suspend fun getWordsForBookList(bookNum: Int): List<WordEntity> = withContext(Dispatchers.IO) {
        wordDao.getWordsByBookList(bookNum)
    }

    suspend fun getWordById(wordId: Long): WordEntity? = withContext(Dispatchers.IO) {
        wordDao.getWordById(wordId)
    }

    fun getAllProgress(): Flow<List<UnitProgressEntity>> {
        return unitProgressDao.getAllProgress()
    }

    fun getProgressForBook(bookNum: Int): Flow<List<UnitProgressEntity>> {
        return unitProgressDao.getProgressForBook(bookNum)
    }

    enum class LocalBookSearchLanguage {
        ALL,
        ENGLISH,
        UZBEK
    }

    fun searchWords(
        query: String,
        language: LocalBookSearchLanguage = LocalBookSearchLanguage.ALL
    ): Flow<List<WordEntity>> {
        val clean = normalizeSearchText(query)
        val cleanNoApos = stripApostrophes(clean)
        if (clean.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return kotlinx.coroutines.flow.flow {
            wordDao.getAllWords().collect { allWords ->
                val scored = withContext(Dispatchers.Default) {
                    allWords.mapNotNull { entity ->
                        val rank = scoreWordMatch(entity, clean, cleanNoApos, language)
                        if (rank >= 0) rank to entity else null
                    }.sortedWith(
                        compareBy<Pair<Int, WordEntity>> { it.first }
                            .thenBy { it.second.bookNumber }
                            .thenBy { it.second.unitNumber }
                            .thenBy { it.second.id }
                    ).map { it.second }
                }
                emit(scored)
            }
        }
    }

    private fun scoreWordMatch(
        entity: WordEntity,
        clean: String,
        cleanNoApos: String,
        language: LocalBookSearchLanguage
    ): Int {
        val wordNorm = normalizeSearchText(entity.word)
        val uzTrNorm = normalizeSearchText(entity.uzbekTranslation)
        val uzTrNoApos = stripApostrophes(uzTrNorm)
        val uzTokens = uzTrNorm.split(",", ";", "/").map { normalizeSearchText(it) }.filter { it.isNotBlank() }
        val uzTokensNoApos = uzTokens.map { stripApostrophes(it) }

        return when (language) {
            LocalBookSearchLanguage.ENGLISH -> {
                when {
                    wordNorm == clean -> 0
                    wordNorm.startsWith(clean) -> 1
                    wordNorm.contains(clean) -> 2
                    else -> -1
                }
            }
            LocalBookSearchLanguage.UZBEK -> {
                when {
                    uzTokens.any { it == clean } || (cleanNoApos.isNotBlank() && uzTokensNoApos.any { it == cleanNoApos }) -> 0
                    uzTokens.any { it.startsWith(clean) } || (cleanNoApos.isNotBlank() && uzTokensNoApos.any { it.startsWith(cleanNoApos) }) -> 1
                    uzTrNorm.contains(clean) || (cleanNoApos.isNotBlank() && uzTrNoApos.contains(cleanNoApos)) -> 2
                    else -> -1
                }
            }
            else -> {
                val enRank = when {
                    wordNorm == clean -> 0
                    wordNorm.startsWith(clean) -> 2
                    else -> -1
                }
                val uzRank = when {
                    uzTokens.any { it == clean } -> 1
                    else -> -1
                }
                minOf(if (enRank >= 0) enRank else 99, if (uzRank >= 0) uzRank else 99).takeIf { it != 99 } ?: -1
            }
        }
    }

    private fun normalizeSearchText(text: String): String {
        return text.trim()
            .lowercase(java.util.Locale.ROOT)
            .replace("ʻ", "'")
            .replace("ʼ", "'")
            .replace("’", "'")
            .replace("‘", "'")
            .replace("`", "'")
            .replace("′", "'")
            .replace("ʹ", "'")
    }

    private fun stripApostrophes(normalizedText: String): String {
        return normalizedText.replace("'", "")
    }

    fun getSynonymsAndAntonyms(): Flow<List<WordEntity>> {
        return wordDao.getWordsWithSynonymsOrAntonyms()
    }

    fun getLearnedWords(): Flow<List<WordEntity>> {
        return wordDao.getLearnedWords()
    }

    fun getLearnedCount(): Flow<Int> {
        return wordDao.getLearnedCount()
    }

    fun getBookmarkedWords(): Flow<List<WordEntity>> {
        return wordDao.getBookmarkedWords()
    }

    fun getBookmarkedCount(): Flow<Int> {
        return wordDao.getBookmarkedCount()
    }

    fun getLeaderboard(): Flow<List<LeaderboardEntity>> {
        return leaderboardDao.getTopScores(10)
    }

    fun getTop5Leaderboard(): Flow<List<LeaderboardEntity>> {
        return leaderboardDao.getTop5Scores()
    }

    suspend fun enterFinalExam(bookNumber: Int) = withContext(Dispatchers.IO) {
        // Automatically lock all units of that book to prevent cheating during the Final Exam
        unitProgressDao.lockAllUnitsForBook(bookNumber)
    }

    suspend fun unlockAllUnitsForBook(bookNumber: Int) = withContext(Dispatchers.IO) {
        unitProgressDao.unlockAllUnitsForBook(bookNumber)
    }

    fun getSearchHistory(limit: Int = 10): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getRecentSearches(limit)
    }

    suspend fun saveSearchQuery(query: String) = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            searchHistoryDao.deleteSearchByQuery(trimmed)
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = trimmed, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun deleteSearchQuery(id: Long) = withContext(Dispatchers.IO) {
        searchHistoryDao.deleteSearchById(id)
    }

    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        searchHistoryDao.clearAll()
    }

    fun getDailyStreak(): Int {
        return StreakManager.getStreak(context)
    }

    suspend fun updateWordLearned(wordId: Long, isLearned: Boolean) {
        val timestamp = if (isLearned) System.currentTimeMillis() else 0L
        wordDao.updateLearnedStatus(wordId, isLearned, timestamp)
        if (isLearned) {
            StreakManager.recordUnitCompletionToday(context)
        }
    }

    suspend fun updateWordBookmarked(wordId: Long, isBookmarked: Boolean) {
        wordDao.updateBookmarkStatus(wordId, isBookmarked)
    }

    suspend fun passUnit(bookNum: Int, unitNum: Int, score: Int) {
        unitProgressDao.markUnitPassed(bookNum, unitNum, score)
        if (unitNum < 30) {
            unitProgressDao.unlockUnit(bookNum, unitNum + 1)
        } else if (bookNum < 6) {
            unitProgressDao.unlockUnit(bookNum + 1, 1)
        }
        // Record streak
        StreakManager.recordUnitCompletionToday(context)

        // Record into local leaderboard
        leaderboardDao.insertScore(
            LeaderboardEntity(
                userName = "Study Master",
                bookNumber = bookNum,
                unitNumber = unitNum,
                score = score,
                totalQuestions = 10,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordQuizScore(userName: String, bookNum: Int, unitNum: Int, score: Int, totalQuestions: Int = 10) {
        StreakManager.recordUnitCompletionToday(context)
        leaderboardDao.insertScore(
            LeaderboardEntity(
                userName = userName.ifBlank { "Study Master" },
                bookNumber = bookNum,
                unitNumber = unitNum,
                score = score,
                totalQuestions = totalQuestions,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Room caching layer for Gemini API questions to support offline practice.
     */
    suspend fun generateAiQuiz(word: String, definition: String): String = withContext(Dispatchers.IO) {
        // 1. Check local Room cache first (Instant & Offline available)
        val cached = quizCacheDao.getCachedQuiz(word)
        if (cached != null) {
            return@withContext "Question: ${cached.question}\nOptions:\nA) ${cached.optionA}\nB) ${cached.optionB}\nC) ${cached.optionC}\nD) ${cached.optionD}\nCorrect Answer: ${cached.correctAnswer}\n\nExplanation: ${cached.explanation}"
        }

        // 2. Fetch from Gemini API
        val aiResponse = geminiHelper.generateQuizQuestion(word, definition)

        // 3. Store into Room Cache for future offline practice
        try {
            val entity = QuizCacheEntity(
                word = word,
                question = "What is the accurate definition of '$word'?",
                optionA = definition,
                optionB = "A completely different meaning unrelated to $word",
                optionC = "A temporary or passing physical condition",
                optionD = "An ancient linguistic dialect phrase",
                correctAnswer = "A",
                explanation = "The word '$word' means '$definition'.",
                cachedTimestamp = System.currentTimeMillis()
            )
            quizCacheDao.insertQuiz(entity)
        } catch (e: Exception) {
            Log.e("VocabRepo", "Error saving quiz to cache", e)
        }

        aiResponse
    }

    suspend fun getAiExplanation(word: String, definition: String): String = withContext(Dispatchers.IO) {
        // Disabled: fetch definition from static dataset only, not dynamic API
        "Definiton: $definition"
    }

    suspend fun exportProgressAsJson(): String = withContext(Dispatchers.IO) {
        val progressList = unitProgressDao.getAllProgressList()
        val learnedWords = wordDao.getAllWordsList().filter { it.isLearned }
        val leaderboardScores = leaderboardDao.getTopScores(50)

        val root = JSONObject()
        root.put("exportDate", System.currentTimeMillis())
        root.put("app", "4000 Essential English Words")
        root.put("streakDays", StreakManager.getStreak(context))
        root.put("totalLearnedWordsCount", learnedWords.size)
        root.put("totalPassedUnitsCount", progressList.count { it.isPassed })

        val unitsArray = JSONArray()
        for (u in progressList) {
            val uObj = JSONObject()
            uObj.put("bookNumber", u.bookNumber)
            uObj.put("unitNumber", u.unitNumber)
            uObj.put("unitTitle", u.unitTitle)
            uObj.put("isUnlocked", u.isUnlocked)
            uObj.put("isPassed", u.isPassed)
            uObj.put("score", u.score)
            unitsArray.put(uObj)
        }
        root.put("unitProgress", unitsArray)

        val wordsArray = JSONArray()
        for (w in learnedWords) {
            val wObj = JSONObject()
            wObj.put("id", w.id)
            wObj.put("bookNumber", w.bookNumber)
            wObj.put("unitNumber", w.unitNumber)
            wObj.put("word", w.word)
            wObj.put("uzbekTranslation", w.uzbekTranslation)
            wObj.put("learnedTimestamp", w.learnedTimestamp)
            wordsArray.put(wObj)
        }
        root.put("learnedWords", wordsArray)

        root.toString(2)
    }

    suspend fun exportProgressAsCsv(): String = withContext(Dispatchers.IO) {
        val progressList = unitProgressDao.getAllProgressList()
        val learnedWords = wordDao.getAllWordsList().filter { it.isLearned }

        val sb = StringBuilder()
        sb.append("Type,BookNumber,UnitNumber,UnitTitleOrWord,UzbekTranslation,IsPassedOrLearned,Score,Timestamp\n")
        for (u in progressList) {
            sb.append("Unit,${u.bookNumber},${u.unitNumber},\"${u.unitTitle}\",,${u.isPassed},${u.score},\n")
        }
        for (w in learnedWords) {
            sb.append("Word,${w.bookNumber},${w.unitNumber},\"${w.word}\",\"${w.uzbekTranslation}\",true,,${w.learnedTimestamp}\n")
        }
        sb.toString()
    }

    enum class ReviewGrade {
        AGAIN, GOOD, EASY
    }

    fun getAllWords(): Flow<List<WordEntity>> {
        return wordDao.getAllWords()
    }

    fun getDueWords(): Flow<List<WordEntity>> {
        return wordDao.getDueWords()
    }

    fun getDueWordsCount(): Flow<Int> {
        return wordDao.getDueWordsCount()
    }

    suspend fun reviewWord(wordId: Long, grade: ReviewGrade) = withContext(Dispatchers.IO) {
        val word = wordDao.getWordById(wordId) ?: return@withContext
        var interval = word.repetitionIntervalDays
        var repCount = word.repetitionCount
        var ease = word.easeFactor

        when (grade) {
            ReviewGrade.AGAIN -> {
                interval = 1
                repCount = 0
                ease = maxOf(1.3f, ease - 0.2f)
            }
            ReviewGrade.GOOD -> {
                if (repCount == 0) {
                    interval = 1
                } else if (repCount == 1) {
                    interval = 3
                } else {
                    interval = maxOf(1, (interval * ease).toInt())
                }
                repCount++
                ease = maxOf(1.3f, ease + 0.05f)
            }
            ReviewGrade.EASY -> {
                if (repCount == 0) {
                    interval = 4
                } else {
                    interval = maxOf(1, (interval * ease * 1.3f).toInt())
                }
                repCount++
                ease = maxOf(1.3f, ease + 0.15f)
            }
        }

        val nextReview = System.currentTimeMillis() + (interval.toLong() * 24L * 3600L * 1000L)
        wordDao.updateSpacedRepetition(wordId, nextReview, interval, repCount, ease)
        wordDao.updateLearnedStatus(wordId, true)
    }
}
