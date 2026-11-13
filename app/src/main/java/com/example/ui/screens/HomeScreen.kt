package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.dictionary.SearchDirection
import com.example.data.local.WordEntity
import com.example.ui.components.AdaptiveSidebarScaffold
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.GlobalDictionaryEntryCard
import com.example.ui.components.SidebarDestination
import com.example.ui.components.SidebarToggleIconButton
import com.example.ui.components.glassFilterChipBorder
import com.example.ui.components.glassFilterChipColors
import com.example.ui.components.glassTextFieldColors
import com.example.ui.theme.GlassAmberAccent
import com.example.ui.theme.GlassAmberBorder
import com.example.ui.theme.GlassAmberFill
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassEmeraldAccent
import com.example.ui.theme.GlassEmeraldBorder
import com.example.ui.theme.GlassEmeraldFill
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.GlassRoyalBlueAccent
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassVioletAccent
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.GlobalSearchViewModel
import com.example.viewmodel.VocabularyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    vocabularyViewModel: VocabularyViewModel,
    globalSearchViewModel: GlobalSearchViewModel,
    onOpenEssentialBooksClick: () -> Unit,
    onBookClick: (Int) -> Unit = {},
    onBookSearchClick: () -> Unit = {},
    onSynonymsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onSpacedRepetitionClick: () -> Unit,
    onWordClick: (Long) -> Unit = {},
    onSavedWordsClick: () -> Unit = {}
) {
    val searchUiState by globalSearchViewModel.uiState.collectAsStateWithLifecycle()
    val learnedCount by vocabularyViewModel.learnedCount.collectAsStateWithLifecycle()
    val dueCount by vocabularyViewModel.dueWordsCount.collectAsStateWithLifecycle()
    val streak by vocabularyViewModel.dailyStreak.collectAsStateWithLifecycle()
    val bookmarkedWords by vocabularyViewModel.bookmarkedWords.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val sampleQueries = if (searchUiState.direction == SearchDirection.ENG_TO_UZB) {
        listOf("serendipity", "ubiquitous", "algorithm", "ephemeral", "resilient", "sustainability")
    } else {
        listOf("kitob", "do'st", "muvaffaqiyat", "kelajak", "bilim", "harakat")
    }

    AdaptiveSidebarScaffold(
        activeDestination = SidebarDestination.HOME,
        dueCount = dueCount,
        learnedCount = learnedCount,
        onNavigateHome = {},
        onNavigateEssentialBooks = onOpenEssentialBooksClick,
        onNavigateBookSearch = onBookSearchClick,
        onNavigateSpacedRepetition = onSpacedRepetitionClick,
        onNavigateSynonyms = onSynonymsClick,
        onNavigateDashboard = onDashboardClick,
        onNavigateProfile = onProfileClick,
        onQuickBookSelect = onBookClick
    ) { onToggleSidebar, isSidebarOpen, isWideScreen ->
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopAppBar(
                    navigationIcon = {
                        SidebarToggleIconButton(
                            isSidebarOpen = isSidebarOpen,
                            onToggleClick = onToggleSidebar
                        )
                    },
                    title = {
                        Column {
                            Text(
                                text = "4000 Essential Words",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Global Dictionary & Curriculum",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMetaTranslucent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        if (!isWideScreen || !isSidebarOpen) {
                            IconButton(
                                onClick = onProfileClick,
                                modifier = Modifier.testTag("home_profile_button")
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Profile & Stats", tint = TextPrimaryWhite)
                            }
                        }
                    }
                )
            }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 310.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LAYER 1: Independent Bidirectional Global Dictionary Search Bar (Tier 2 Glass Card)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 22.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_global_search_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlassPill(
                                    tintColor = GlassCyanFill,
                                    borderColor = GlassCyanBorder,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = GlassCyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Global English ↔ Uzbek Dictionary",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimaryWhite
                                )
                            }

                            OutlinedTextField(
                                value = searchUiState.query,
                                onValueChange = { globalSearchViewModel.onQueryChange(it) },
                                placeholder = {
                                    val searchPlaceholder = if (searchUiState.direction == SearchDirection.ENG_TO_UZB) {
                                        "Search English word..."
                                    } else {
                                        "O'zbekcha so'z qidirish..."
                                    }
                                    Text(
                                        text = searchPlaceholder,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = TextMetaTranslucent
                                    )
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = GlassCyanAccent
                                    )
                                },
                                trailingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        if (searchUiState.query.isNotEmpty()) {
                                            IconButton(
                                                onClick = { globalSearchViewModel.onQueryChange("") },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("home_clear_global_search")
                                            ) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextBodyTranslucent, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        val directionLabel = if (searchUiState.direction == SearchDirection.ENG_TO_UZB) "ENG ⇄ UZB" else "UZB ⇄ ENG"
                                        GlassPill(
                                            tintColor = if (searchUiState.direction == SearchDirection.ENG_TO_UZB) GlassCyanFill else GlassVioletFill,
                                            borderColor = if (searchUiState.direction == SearchDirection.ENG_TO_UZB) GlassCyanBorder else GlassVioletBorder,
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { globalSearchViewModel.swapDirection() },
                                            modifier = Modifier
                                                .height(32.dp)
                                                .testTag("home_direction_toggle_pill")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = directionLabel,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = TextPrimaryWhite
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                globalSearchViewModel.submitSearch()
                                                focusManager.clearFocus()
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("home_submit_global_search")
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Search dictionary",
                                                tint = GlassCyanAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    globalSearchViewModel.submitSearch()
                                    focusManager.clearFocus()
                                }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_global_search_input"),
                                shape = RoundedCornerShape(16.dp),
                                colors = glassTextFieldColors()
                            )

                             if (searchUiState.isLoading) {
                                GlassLinearProgress(
                                    progress = 0.7f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_global_search_loading"),
                                    height = 5.dp
                                )
                            }

                            if (searchUiState.recentSearches.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_recent_lookups_section"),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                                text = "Recent Global Lookups (${searchUiState.recentSearches.take(5).size}/5)",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimaryWhite
                                            )
                                        }
                                        TextButton(
                                            onClick = { globalSearchViewModel.clearRecentSearches() },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("home_clear_recent_lookups")
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
                                        searchUiState.recentSearches.take(5).forEach { recent ->
                                            val isSelected = recent.equals(searchUiState.query.trim(), ignoreCase = true)
                                            Surface(
                                                onClick = {
                                                    globalSearchViewModel.revisitRecentSearch(recent)
                                                    focusManager.clearFocus()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) GlassCyanFill else GlassTier3Fill,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) GlassCyanBorder else GlassTier3Border
                                                ),
                                                modifier = Modifier.testTag("home_recent_lookup_$recent")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = recent,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        ),
                                                        color = if (isSelected) GlassCyanAccent else TextPrimaryWhite
                                                    )
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Remove $recent",
                                                        tint = TextMetaTranslucent,
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { globalSearchViewModel.deleteRecentSearch(recent) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (searchUiState.query.isBlank()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Try searching any English or Uzbek word:",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = GlassCyanAccent
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        sampleQueries.forEach { sample ->
                                            Surface(
                                                onClick = {
                                                    globalSearchViewModel.submitSearch(sample)
                                                    focusManager.clearFocus()
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                color = GlassTier3Fill,
                                                border = BorderStroke(1.dp, GlassTier3Border),
                                                modifier = Modifier.testTag("home_sample_query_$sample")
                                            ) {
                                                Text(
                                                    text = sample,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = TextPrimaryWhite,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (searchUiState.query.isNotBlank()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        GlassCard(
                            tier = GlassTier.TIER_3,
                            cornerRadius = 14.dp,
                            tintColor = GlassRoyalBlueFill,
                            borderColor = GlassRoyalBlueBorder,
                            onClick = { onOpenEssentialBooksClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("essential_english_words_all_compact")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = GlassCyanAccent
                                    )
                                    Text(
                                        text = "Essential English Words All (Books 1–6)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Essential English Words All",
                                    tint = GlassCyanAccent
                                )
                            }
                        }
                    }

                    when {
                        searchUiState.errorMessage != null -> {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                GlassCard(
                                    tier = GlassTier.TIER_2,
                                    cornerRadius = 18.dp,
                                    tintColor = GlassRoseFill,
                                    borderColor = GlassRoseBorder,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_global_search_error_card")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.SearchOff,
                                            contentDescription = null,
                                            tint = GlassRoseAccent,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = searchUiState.errorMessage ?: "Dictionary lookup error",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimaryWhite,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.testTag("home_global_search_error_text")
                                        )
                                    }
                                }
                            }
                        }

                        !searchUiState.isLoading && searchUiState.hasSearched && searchUiState.entries.isEmpty() -> {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                GlassCard(
                                    tier = GlassTier.TIER_2,
                                    cornerRadius = 18.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = GlassRoseAccent
                                        )
                                        Text(
                                            text = "No dictionary entry found for \"${searchUiState.query.trim()}\"",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimaryWhite,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (searchUiState.direction == SearchDirection.ENG_TO_UZB) {
                                                "Check spelling or switch to UZB ➔ ENG if you entered an Uzbek word."
                                            } else {
                                                "Try another Uzbek word or switch to ENG ➔ UZB if you entered an English word."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextBodyTranslucent,
                                            textAlign = TextAlign.Center
                                        )
                                        if (searchUiState.suggestions.isNotEmpty()) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                searchUiState.suggestions.forEach { suggestion ->
                                                    Surface(
                                                        onClick = {
                                                            globalSearchViewModel.selectSuggestion(suggestion)
                                                            focusManager.clearFocus()
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = GlassTier3Fill,
                                                        border = BorderStroke(1.dp, GlassTier3Border)
                                                    ) {
                                                        Text(
                                                            text = suggestion,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = TextPrimaryWhite,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            if (searchUiState.suggestions.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Related terms",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextMetaTranslucent
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            searchUiState.suggestions.forEach { suggestion ->
                                                Surface(
                                                    onClick = {
                                                        globalSearchViewModel.selectSuggestion(suggestion)
                                                        focusManager.clearFocus()
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = GlassTier3Fill,
                                                    border = BorderStroke(1.dp, GlassTier3Border)
                                                ) {
                                                    Text(
                                                        text = suggestion,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = TextPrimaryWhite,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            items(
                                items = searchUiState.entries,
                                key = { "${it.tier}_${it.direction}_${it.headword}_${it.matchedFromUzbek ?: ""}_${it.geoInfo?.entityType ?: ""}" }
                            ) { entry ->
                                GlobalDictionaryEntryCard(
                                    entry = entry,
                                    onPronounceUs = {
                                        globalSearchViewModel.pronounceUs(entry.headword, entry.audioUrlUs)
                                    },
                                    onPronounceUk = {
                                        globalSearchViewModel.pronounceUk(entry.headword, entry.audioUrlUk)
                                    },
                                    onRelatedWordClick = { word ->
                                        globalSearchViewModel.lookupEnglishWord(word)
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Prominent Navigation Card: Essential English Words All (Luminous Tier 2 Glass Hero)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val masteryFraction = (learnedCount.toFloat() / 3600f).coerceIn(0f, 1f)
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 24.dp,
                            tintColor = GlassRoyalBlueFill,
                            borderColor = GlassCyanBorder,
                            onClick = { onOpenEssentialBooksClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("essential_english_words_all_card")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                GlassCyanFill,
                                                GlassVioletFill
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        GlassSurfaceBox(
                                            tier = GlassTier.TIER_3,
                                            cornerRadius = 16.dp,
                                            tintColor = GlassCyanFill,
                                            borderColor = GlassCyanBorder,
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.size(54.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                contentDescription = "Essential English Words All",
                                                tint = GlassCyanAccent,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(
                                                text = "Essential English Words All",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = TextPrimaryWhite
                                            )
                                            Text(
                                                text = "Books 1–6 • 180 Units • 3,600 Target Words",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextBodyTranslucent
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Course Mastery",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextBodyTranslucent
                                            )
                                            Text(
                                                text = "$learnedCount / 3,600 words",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = GlassCyanAccent
                                            )
                                        }
                                        GlassLinearProgress(
                                            progress = masteryFraction,
                                            modifier = Modifier.fillMaxWidth(),
                                            height = 8.dp
                                        )
                                    }

                                    GlassActionButton(
                                        onClick = onOpenEssentialBooksClick,
                                        tintColor = GlassCyanFill,
                                        borderColor = GlassCyanBorder,
                                        cornerRadius = 14.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("open_essential_books_button")
                                    ) {
                                        Text(
                                            text = "Open Essential English Words All",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimaryWhite
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = GlassCyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }


                    item {
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 18.dp,
                            tintColor = GlassEmeraldFill,
                            borderColor = GlassEmeraldBorder,
                            onClick = { onDashboardClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_mastered_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Mastered words",
                                        tint = GlassEmeraldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mastered",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextBodyTranslucent
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$learnedCount",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = GlassEmeraldAccent
                                )
                                Text(
                                    text = "of 3,600 words",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMetaTranslucent
                                )
                            }
                        }
                    }

                    item {
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 18.dp,
                            tintColor = GlassAmberFill,
                            borderColor = GlassAmberBorder,
                            onClick = { onDashboardClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_streak_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Daily Streak",
                                        tint = GlassAmberAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Daily Streak",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextBodyTranslucent
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$streak Days",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = GlassAmberAccent
                                )
                                Text(
                                    text = if (streak > 0) "Keep studying!" else "Start today!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMetaTranslucent
                                )
                            }
                        }
                    }

                    // MY SAVED WORDS FROSTED-GLASS SECTION
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 22.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_my_saved_words_section")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GlassPill(
                                            tintColor = GlassAmberFill,
                                            borderColor = GlassAmberBorder,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = GlassAmberAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "My Saved Words",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimaryWhite
                                            )
                                            Text(
                                                text = "${bookmarkedWords.size} saved vocabulary ${if (bookmarkedWords.size == 1) "word" else "words"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMetaTranslucent
                                            )
                                        }
                                    }

                                    GlassPill(
                                        tintColor = GlassAmberFill,
                                        borderColor = GlassAmberBorder,
                                        shape = RoundedCornerShape(10.dp),
                                        onClick = onSavedWordsClick,
                                        modifier = Modifier.testTag("home_view_all_saved_words_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (bookmarkedWords.isEmpty()) "Browse" else "View All (${bookmarkedWords.size})",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = GlassAmberAccent
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Open Saved Words",
                                                tint = GlassAmberAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                if (bookmarkedWords.isEmpty()) {
                                    GlassSurfaceBox(
                                        tier = GlassTier.TIER_3,
                                        cornerRadius = 16.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("home_saved_words_empty_state")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BookmarkBorder,
                                                contentDescription = null,
                                                tint = GlassAmberAccent,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "No saved words yet",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = TextPrimaryWhite
                                                )
                                                Text(
                                                    text = "Tap the bookmark icon on any vocabulary word across Books 1–6 or search results to save it here for quick review.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextBodyTranslucent
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        bookmarkedWords.take(5).forEach { savedWord ->
                                            HomeSavedWordRow(
                                                word = savedWord,
                                                onWordClick = { onWordClick(savedWord.id) },
                                                onSpeakUs = { vocabularyViewModel.speakWordWithAccent(savedWord.word, false) },
                                                onSpeakUk = { vocabularyViewModel.speakWordWithAccent(savedWord.word, true) },
                                                onRemoveBookmark = {
                                                    vocabularyViewModel.toggleBookmark(savedWord.id, false)
                                                }
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
    }
}

@Composable
private fun HomeSavedWordRow(
    word: WordEntity,
    onWordClick: () -> Unit,
    onSpeakUs: () -> Unit,
    onSpeakUk: () -> Unit,
    onRemoveBookmark: () -> Unit
) {
    GlassCard(
        tier = GlassTier.TIER_3,
        cornerRadius = 16.dp,
        onClick = onWordClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_saved_word_card_${word.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimaryWhite
                    )
                    GlassPill(
                        tintColor = GlassRoyalBlueFill,
                        borderColor = GlassRoyalBlueBorder,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = word.partOfSpeech,
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassCyanAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (word.phonetic.isNotBlank()) {
                        Text(
                            text = word.phonetic,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMetaTranslucent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DualAudioButtons(
                        word = word.word,
                        onSpeakUs = onSpeakUs,
                        onSpeakUk = onSpeakUk
                    )
                    IconButton(
                        onClick = onRemoveBookmark,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("home_saved_word_unbookmark_${word.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove from Saved Words",
                            tint = GlassAmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🇺🇿 ${word.uzbekTranslation.ifBlank { word.definition }}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = GlassCyanAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                GlassPill(
                    tintColor = GlassTier3Fill,
                    borderColor = GlassTier3Border,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "B${word.bookNumber} • U${word.unitNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMetaTranslucent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
