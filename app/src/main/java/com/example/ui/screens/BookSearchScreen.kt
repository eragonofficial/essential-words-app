package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.TextbookSearchResultCard
import com.example.ui.components.glassFilterChipBorder
import com.example.ui.components.glassFilterChipColors
import com.example.ui.components.glassTextFieldColors
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel
import java.util.Locale

@Composable
fun BookSearchScreen(
    viewModel: VocabularyViewModel,
    onWordClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    onUnitClick: (Int, Int) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val learnedWords by viewModel.learnedWords.collectAsStateWithLifecycle()
    val bookmarkedWords by viewModel.bookmarkedWords.collectAsStateWithLifecycle()
    val dynamicThesaurusMap by viewModel.dynamicThesaurusMap.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Learned", "Bookmarked", "Book 1", "Book 2", "Book 3", "Book 4", "Book 5", "Book 6")
    val focusManager = LocalFocusManager.current

    fun performSearch(query: String) {
        val trimmed = query.trim()
        searchQuery = trimmed
        viewModel.search(trimmed)
        if (trimmed.isNotBlank()) {
            viewModel.recordSearch(trimmed)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.search(it)
                        },
                        placeholder = {
                            Text(
                                "Search Books 1–6 (ENG or O'zbekcha)...",
                                color = TextMetaTranslucent
                            )
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.search("")
                                    },
                                    modifier = Modifier.testTag("clear_book_search_button")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear book search", tint = TextBodyTranslucent)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotBlank()) {
                                performSearch(searchQuery)
                            }
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                            .testTag("book_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = glassTextFieldColors()
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("book_search_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryWhite)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (searchHistory.isNotEmpty()) {
                GlassSurfaceBox(
                    tier = GlassTier.TIER_1,
                    cornerRadius = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = GlassCyanAccent
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Recent Book Searches",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassCyanAccent
                                )
                            }
                            TextButton(
                                onClick = { viewModel.clearSearchHistory() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall, color = GlassCyanAccent)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            searchHistory.forEach { history ->
                                Surface(
                                    onClick = { performSearch(history.query) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = GlassTier3Fill,
                                    border = BorderStroke(1.dp, GlassTier3Border)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(history.query, style = MaterialTheme.typography.labelMedium, color = TextPrimaryWhite)
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Delete search history item",
                                            tint = TextMetaTranslucent,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.deleteSearch(history.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = glassFilterChipColors(),
                        border = glassFilterChipBorder(selectedCategory == category),
                        modifier = Modifier.testTag("book_filter_chip_$category")
                    )
                }
            }

            val displayedResults = remember(searchResults, learnedWords, bookmarkedWords, searchQuery, selectedCategory) {
                val list = when (selectedCategory) {
                    "Learned" -> learnedWords
                    "Bookmarked" -> bookmarkedWords
                    "Book 1" -> if (searchQuery.isNotBlank()) searchResults.filter { it.bookNumber == 1 } else emptyList()
                    "Book 2" -> if (searchQuery.isNotBlank()) searchResults.filter { it.bookNumber == 2 } else emptyList()
                    "Book 3" -> if (searchQuery.isNotBlank()) searchResults.filter { it.bookNumber == 3 } else emptyList()
                    "Book 4" -> if (searchQuery.isNotBlank()) searchResults.filter { it.bookNumber == 4 } else emptyList()
                    "Book 5" -> if (searchQuery.isNotBlank()) searchResults.filter { it.bookNumber == 5 } else emptyList()
                    "Book 6" -> if (searchQuery.isNotBlank()) searchResults.filter { it.bookNumber == 6 } else emptyList()
                    else -> searchResults
                }

                if (selectedCategory == "Learned" || selectedCategory == "Bookmarked") {
                    if (searchQuery.isNotBlank()) {
                        list.filter {
                            it.word.contains(searchQuery, ignoreCase = true) ||
                                it.uzbekTranslation.contains(searchQuery, ignoreCase = true) ||
                                it.uzbekDefinition.contains(searchQuery, ignoreCase = true) ||
                                it.definition.contains(searchQuery, ignoreCase = true) ||
                                it.englishDefinition.contains(searchQuery, ignoreCase = true)
                        }
                    } else {
                        list
                    }
                } else {
                    list
                }
            }

            if (searchQuery.isBlank() && displayedResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 22.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            GlassSurfaceBox(
                                tier = GlassTier.TIER_3,
                                cornerRadius = 20.dp,
                                tintColor = GlassCyanFill,
                                borderColor = GlassCyanBorder,
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(38.dp),
                                    tint = GlassCyanAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when (selectedCategory) {
                                    "Learned" -> "No learned words yet"
                                    "Bookmarked" -> "No bookmarked words yet"
                                    else -> "Essential English Words All"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryWhite
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (selectedCategory) {
                                    "Learned" -> "Words you mark as learned in unit study will appear here."
                                    "Bookmarked" -> "Words you bookmark will appear here for fast review."
                                    else -> "Search inside the Essential English Words books (3,600 textbook words across Books 1–6 & 180 Units)."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextBodyTranslucent,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else if (displayedResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 22.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = GlassRoseAccent
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "No textbook words found for \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryWhite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Try another English or Uzbek keyword, or use the Home Global Dictionary for general lookups.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextBodyTranslucent,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Found ${displayedResults.size} textbook words ($selectedCategory)",
                            style = MaterialTheme.typography.labelMedium,
                            color = GlassCyanAccent,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(displayedResults, key = { it.id }) { wordEntity ->
                        val thesaurus = dynamicThesaurusMap[wordEntity.word.trim().lowercase(Locale.ROOT)]
                        TextbookSearchResultCard(
                            wordEntity = wordEntity,
                            searchQuery = searchQuery,
                            dynamicThesaurus = thesaurus,
                            onRequestThesaurus = { viewModel.ensureDynamicThesaurus(it) },
                            onWordClick = onWordClick,
                            onSpeakUk = { viewModel.speakWordWithAccent(it, isUk = true) },
                            onSpeakUs = { viewModel.speakWordWithAccent(it, isUk = false) },
                            onRelatedWordClick = { related ->
                                performSearch(related)
                                focusManager.clearFocus()
                            },
                            onBookUnitClick = onUnitClick,
                            onToggleBookmark = { id, bookmarked ->
                                viewModel.toggleBookmark(id, bookmarked)
                            }
                        )
                    }
                }
            }
        }
    }
}
