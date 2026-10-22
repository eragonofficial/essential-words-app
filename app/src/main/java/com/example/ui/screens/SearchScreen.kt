package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val learnedWords by viewModel.learnedWords.collectAsState()
    val bookmarkedWords by viewModel.bookmarkedWords.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Learned", "Bookmarked", "Book 1", "Book 2", "Book 3", "Book 4")
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
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.search(it)
                        },
                        placeholder = { Text("Search all 4000 words...") },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.search("")
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
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
                            .testTag("global_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search History Chips Section
            if (searchHistory.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Recent Searches",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            TextButton(
                                onClick = { viewModel.clearSearchHistory() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            searchHistory.forEach { history ->
                                InputChip(
                                    selected = false,
                                    onClick = { performSearch(history.query) },
                                    label = { Text(history.query) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(16.dp).clickable { viewModel.deleteSearch(history.id) })
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Quick Category Filters
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
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_$category")
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
                    else -> searchResults
                }

                if (selectedCategory == "Learned" || selectedCategory == "Bookmarked") {
                    if (searchQuery.isNotBlank()) {
                        list.filter {
                            it.word.contains(searchQuery, ignoreCase = true) ||
                                    it.uzbekTranslation.contains(searchQuery, ignoreCase = true) ||
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

            // Search Content / Results
            if (searchQuery.isBlank() && displayedResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedCategory == "Learned") "No learned words yet"
                            else if (selectedCategory == "Bookmarked") "No bookmarked words yet"
                            else "Instant Vocabulary Search",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedCategory == "Learned") "Words you mark as learned in unit study will appear here."
                            else if (selectedCategory == "Bookmarked") "Words you bookmark will appear here for fast review."
                            else "Type any English word, English definition, or Uzbek translation to search across all books and units.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (displayedResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "No results found for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Check spelling or try a broader search term in category '$selectedCategory'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
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
                            "Found ${displayedResults.size} words ($selectedCategory)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(displayedResults, key = { it.id }) { wordEntity ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = highlightQuery(wordEntity.word, searchQuery),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (wordEntity.phonetic.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = wordEntity.phonetic,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                "B${wordEntity.bookNumber} U${wordEntity.unitNumber}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { viewModel.speakWord(wordEntity.word) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.VolumeUp,
                                                contentDescription = "Pronounce",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                if (wordEntity.uzbekTranslation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🇺🇿 ${wordEntity.uzbekTranslation}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF0D9488)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val def = if (wordEntity.definition.isNotBlank()) wordEntity.definition else wordEntity.englishDefinition
                                if (def.isNotBlank()) {
                                    Text(
                                        text = highlightQuery(def, searchQuery),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun highlightQuery(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    val builder = buildAnnotatedString {
        var lastIndex = 0
        var startIndex = text.indexOf(query, ignoreCase = true)

        while (startIndex != -1) {
            append(text.substring(lastIndex, startIndex))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))) {
                append(text.substring(startIndex, startIndex + query.length))
            }
            lastIndex = startIndex + query.length
            startIndex = text.indexOf(query, lastIndex, ignoreCase = true)
        }
        append(text.substring(lastIndex))
    }
    return builder
}
