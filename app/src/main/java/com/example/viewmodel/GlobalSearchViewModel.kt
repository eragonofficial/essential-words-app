package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.dictionary.GlobalDictionaryRepository
import com.example.data.dictionary.GlobalSearchUiState
import com.example.data.dictionary.SearchDirection
import com.example.util.AudioPronunciationPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class GlobalSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GlobalDictionaryRepository(application)
    private val audioPlayer = AudioPronunciationPlayer(application)

    private val _uiState = MutableStateFlow(
        GlobalSearchUiState(
            recentSearches = repository.getRecentSearches()
        )
    )
    val uiState: StateFlow<GlobalSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        val currentDirection = _uiState.value.direction
        _uiState.update {
            it.copy(
                query = newQuery,
                errorMessage = null
            )
        }
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    entries = emptyList(),
                    suggestions = emptyList(),
                    errorMessage = null,
                    hasSearched = false
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(380)
            executeSearch(newQuery, currentDirection, recordHistory = false)
        }
    }



    fun submitSearch(explicitQuery: String? = null) {
        val targetQuery = (explicitQuery ?: _uiState.value.query).trim()
        searchJob?.cancel()
        if (targetQuery.isBlank()) return

        _uiState.update { it.copy(query = targetQuery, errorMessage = null) }
        val currentDirection = _uiState.value.direction
        searchJob = viewModelScope.launch {
            executeSearch(targetQuery, currentDirection, recordHistory = true)
        }
    }

    fun revisitRecentSearch(recentQuery: String) {
        val clean = recentQuery.trim()
        if (clean.isBlank()) return
        searchJob?.cancel()
        val currentDirection = _uiState.value.direction
        _uiState.update {
            it.copy(
                query = clean,
                errorMessage = null
            )
        }
        searchJob = viewModelScope.launch {
            executeSearch(clean, currentDirection, recordHistory = true)
        }
    }

    fun lookupEnglishWord(word: String) {
        val clean = word.trim()
        if (clean.isBlank()) return
        searchJob?.cancel()
        val currentDirection = _uiState.value.direction
        _uiState.update {
            it.copy(
                query = clean,
                errorMessage = null
            )
        }
        searchJob = viewModelScope.launch {
            executeSearch(clean, currentDirection, recordHistory = true)
        }
    }

    fun setDirection(direction: SearchDirection) {
        if (_uiState.value.direction == direction) return
        _uiState.update { it.copy(direction = direction, errorMessage = null) }
        val currentQuery = _uiState.value.query.trim()
        if (currentQuery.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                executeSearch(currentQuery, direction, recordHistory = false)
            }
        }
    }

    fun swapDirection() {
        val next = if (_uiState.value.direction == SearchDirection.ENG_TO_UZB) {
            SearchDirection.UZB_TO_ENG
        } else {
            SearchDirection.ENG_TO_UZB
        }
        setDirection(next)
    }

    fun selectSuggestion(suggestion: String) {
        submitSearch(suggestion)
    }

    fun pronounce(word: String, audioUrl: String?) {
        audioPlayer.play(word = word, audioUrl = audioUrl)
    }

    fun pronounceUs(word: String, audioUrlUs: String?) {
        audioPlayer.playUs(word, audioUrlUs)
    }

    fun pronounceUk(word: String, audioUrlUk: String?) {
        audioPlayer.playUk(word, audioUrlUk)
    }

    fun deleteRecentSearch(query: String) {
        val updated = repository.deleteRecentSearch(query)
        _uiState.update { it.copy(recentSearches = updated) }
    }

    fun clearRecentSearches() {
        val updated = repository.clearRecentSearches()
        _uiState.update { it.copy(recentSearches = updated) }
    }

    private suspend fun executeSearch(
        query: String,
        direction: SearchDirection,
        recordHistory: Boolean
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val result = repository.search(query = query, direction = direction)
            val isSuccess = result.entries.isNotEmpty() && result.errorMessage == null
            val updatedRecent = if (recordHistory && isSuccess) {
                repository.recordRecentSearch(query, direction)
            } else {
                _uiState.value.recentSearches
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    entries = result.entries,
                    suggestions = result.suggestions,
                    recentSearches = updatedRecent,
                    errorMessage = result.errorMessage,
                    hasSearched = true
                )
            }
            if (!recordHistory && isSuccess) {
                delay(650)
                val settledRecent = repository.recordRecentSearch(query, direction)
                _uiState.update { it.copy(recentSearches = settledRecent) }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Dictionary lookup failed. Check your internet connection.",
                    hasSearched = true
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
