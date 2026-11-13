package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.dictionary.DynamicThesaurus
import com.example.data.dictionary.GlobalDictionaryService
import com.example.data.local.LeaderboardEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.UnitProgressEntity
import com.example.data.local.WordEntity
import com.example.data.repository.VocabularyRepository
import com.example.util.AudioPronunciationPlayer
import com.example.util.TextToSpeechHelper
import com.example.worker.DailyReminderWorker
import com.example.worker.SpacedRepetitionWorker
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VocabularyRepository(application)
    private val ttsHelper = TextToSpeechHelper(application)
    private val audioPlayer = AudioPronunciationPlayer(application)
    private val dictionaryService = GlobalDictionaryService()

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
        }
        scheduleWorkers(application)
    }

    private fun scheduleWorkers(app: Application) {
        val workManager = WorkManager.getInstance(app)
        // Daily practice reminder
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "daily_vocab_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )

        // Spaced repetition interval reviewer (checks 3, 7, 14 days)
        val spacedRepetitionRequest = PeriodicWorkRequestBuilder<SpacedRepetitionWorker>(12, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "spaced_repetition_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            spacedRepetitionRequest
        )
    }

    val allProgress: StateFlow<List<UnitProgressEntity>> = repository.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learnedCount: StateFlow<Int> = repository.getLearnedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bookmarkedCount: StateFlow<Int> = repository.getBookmarkedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val leaderboard: StateFlow<List<LeaderboardEntity>> = repository.getLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val top5Leaderboard: StateFlow<List<LeaderboardEntity>> = repository.getTop5Leaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.getSearchHistory(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailyStreak = MutableStateFlow(repository.getDailyStreak())
    val dailyStreak: StateFlow<Int> = _dailyStreak.asStateFlow()

    fun refreshStreak() {
        _dailyStreak.value = repository.getDailyStreak()
    }

    val synonymsAndAntonyms: StateFlow<List<WordEntity>> = repository.getSynonymsAndAntonyms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learnedWords: StateFlow<List<WordEntity>> = repository.getLearnedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedWords: StateFlow<List<WordEntity>> = repository.getBookmarkedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWords: StateFlow<List<WordEntity>> = repository.getAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueWords: StateFlow<List<WordEntity>> = repository.getDueWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueWordsCount: StateFlow<Int> = repository.getDueWordsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _searchResults = MutableStateFlow<List<WordEntity>>(emptyList())
    val searchResults: StateFlow<List<WordEntity>> = _searchResults.asStateFlow()

    private val _searchLanguage = MutableStateFlow(VocabularyRepository.LocalBookSearchLanguage.ENGLISH)
    val searchLanguage: StateFlow<VocabularyRepository.LocalBookSearchLanguage> = _searchLanguage.asStateFlow()

    private var lastSearchQuery: String = ""
    private var searchJob: Job? = null

    private val _dynamicThesaurusMap = MutableStateFlow<Map<String, DynamicThesaurus>>(emptyMap())
    val dynamicThesaurusMap: StateFlow<Map<String, DynamicThesaurus>> = _dynamicThesaurusMap.asStateFlow()

    private val _currentUnitWords = MutableStateFlow<List<WordEntity>>(emptyList())
    val currentUnitWords: StateFlow<List<WordEntity>> = _currentUnitWords.asStateFlow()

    private val _isUkAccent = MutableStateFlow(false)
    val isUkAccent: StateFlow<Boolean> = _isUkAccent.asStateFlow()

    private val _aiQuizContent = MutableStateFlow("")
    val aiQuizContent: StateFlow<String> = _aiQuizContent.asStateFlow()

    private val _aiExplanation = MutableStateFlow("")
    val aiExplanation: StateFlow<String> = _aiExplanation.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    val currentUser = MutableStateFlow(Firebase.auth.currentUser)

    fun refreshAuthUser() {
        currentUser.value = Firebase.auth.currentUser
    }

    fun ensureUnitsLoaded(bookNumber: Int) {
        viewModelScope.launch {
            repository.ensureUnitsLoaded(bookNumber)
        }
    }

    fun resetAndRebuildDatabase() {
        viewModelScope.launch {
            repository.resetAndRebuildDatabase()
        }
    }

    fun setUkAccent(isUk: Boolean) {
        _isUkAccent.value = isUk
        ttsHelper.setAccent(isUk)
    }

    fun toggleAccent() {
        setUkAccent(!_isUkAccent.value)
    }

    fun loadWordsForUnit(bookNum: Int, unitNum: Int) {
        viewModelScope.launch {
            repository.getWordsForUnit(bookNum, unitNum).collect { words ->
                _currentUnitWords.value = words
            }
        }
    }

    suspend fun getWordById(wordId: Long): WordEntity? {
        return repository.getWordById(wordId)
    }

    fun setSearchLanguage(language: VocabularyRepository.LocalBookSearchLanguage) {
        _searchLanguage.value = language
        if (lastSearchQuery.isNotBlank()) {
            search(lastSearchQuery, language)
        }
    }

    fun search(
        query: String,
        language: VocabularyRepository.LocalBookSearchLanguage = _searchLanguage.value
    ) {
        lastSearchQuery = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            repository.searchWords(query, language).collect { results ->
                _searchResults.value = results
            }
        }
    }

    fun ensureDynamicThesaurus(word: String) {
        val key = word.trim().lowercase(Locale.ROOT)
        if (key.isBlank()) return
        val existing = _dynamicThesaurusMap.value[key]
        if (existing != null && (existing.isLoading || existing.synonyms.isNotEmpty() || existing.antonyms.isNotEmpty())) {
            return
        }
        if (existing != null && !existing.isLoading) {
            return
        }

        _dynamicThesaurusMap.update { it + (key to DynamicThesaurus(isLoading = true)) }
        viewModelScope.launch {
            try {
                val (syns, ants) = coroutineScope {
                    val synDef = async { dictionaryService.fetchDatamuseSynonyms(key) }
                    val antDef = async { dictionaryService.fetchDatamuseAntonyms(key) }
                    val dmSyns = synDef.await()
                    val dmAnts = antDef.await()
                    if (dmSyns.isEmpty() || dmAnts.isEmpty()) {
                        val freeDict = dictionaryService.fetchFreeDictionary(key)
                        val mergedSyns = (dmSyns + (freeDict?.synonyms ?: emptyList()))
                            .distinctBy { it.lowercase(Locale.ROOT) }
                            .take(12)
                        val mergedAnts = (dmAnts + (freeDict?.antonyms ?: emptyList()))
                            .distinctBy { it.lowercase(Locale.ROOT) }
                            .take(12)
                        mergedSyns to mergedAnts
                    } else {
                        dmSyns.take(12) to dmAnts.take(12)
                    }
                }
                _dynamicThesaurusMap.update {
                    it + (key to DynamicThesaurus(synonyms = syns, antonyms = ants, isLoading = false))
                }
            } catch (_: Exception) {
                _dynamicThesaurusMap.update {
                    it + (key to DynamicThesaurus(isLoading = false))
                }
            }
        }
    }

    fun toggleLearned(wordId: Long, isLearned: Boolean) {
        viewModelScope.launch {
            repository.updateWordLearned(wordId, isLearned)
            refreshStreak()
        }
    }

    fun toggleBookmark(wordId: Long, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.updateWordBookmarked(wordId, isBookmarked)
        }
    }

    fun passUnit(bookNum: Int, unitNum: Int, score: Int) {
        viewModelScope.launch {
            repository.passUnit(bookNum, unitNum, score)
            refreshStreak()
        }
    }

    fun recordQuizScore(userName: String, bookNum: Int, unitNum: Int, score: Int, totalQuestions: Int = 10) {
        viewModelScope.launch {
            repository.recordQuizScore(userName, bookNum, unitNum, score, totalQuestions)
            refreshStreak()
        }
    }

    fun speakWord(word: String) {
        speakWordWithAccent(word, _isUkAccent.value)
    }

    fun speakWordWithAccent(word: String, isUk: Boolean) {
        val clean = word.trim()
        if (clean.isBlank()) return
        _isUkAccent.value = isUk
        if (isUk) {
            audioPlayer.playUk(clean, null)
        } else {
            audioPlayer.playUs(clean, null)
        }
    }

    fun generateAiQuizForWord(word: String, definition: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiQuizContent.value = repository.generateAiQuiz(word, definition)
            _isAiLoading.value = false
        }
    }

    fun getAiWordExplanation(word: String, definition: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiExplanation.value = repository.getAiExplanation(word, definition)
            _isAiLoading.value = false
        }
    }

    fun exportBackup(isJson: Boolean, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val content = if (isJson) repository.exportProgressAsJson() else repository.exportProgressAsCsv()
            onResult(content)
        }
    }

    fun recordSearch(query: String) {
        viewModelScope.launch {
            repository.saveSearchQuery(query)
        }
    }

    fun deleteSearch(id: Long) {
        viewModelScope.launch {
            repository.deleteSearchQuery(id)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun enterFinalExam(bookNumber: Int) {
        viewModelScope.launch {
            repository.enterFinalExam(bookNumber)
        }
    }

    fun unlockBookUnits(bookNumber: Int) {
        viewModelScope.launch {
            repository.unlockAllUnitsForBook(bookNumber)
        }
    }

    suspend fun getWordsForBookList(bookNumber: Int): List<WordEntity> {
        return repository.getWordsForBookList(bookNumber)
    }

    fun reviewWord(wordId: Long, grade: VocabularyRepository.ReviewGrade) {
        viewModelScope.launch {
            repository.reviewWord(wordId, grade)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        ttsHelper.shutdown()
    }
}
