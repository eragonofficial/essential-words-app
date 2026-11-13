package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Repeat
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.VocabularyRepository.LocalBookSearchLanguage
import com.example.ui.components.AdaptiveSidebarScaffold
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.SidebarDestination
import com.example.ui.components.SidebarToggleIconButton
import com.example.ui.components.TextbookSearchResultCard
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
import com.example.ui.theme.GlassPinkAccent
import com.example.ui.theme.GlassPinkBorder
import com.example.ui.theme.GlassPinkFill
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoyalBlueAccent
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassVioletAccent
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookListScreen(
    viewModel: VocabularyViewModel,
    onBookClick: (Int) -> Unit,
    onUnitClick: (Int, Int) -> Unit = { _, _ -> },
    onWordClick: (Long) -> Unit = {},
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSynonymsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onDashboardClick: () -> Unit = {},
    onSpacedRepetitionClick: () -> Unit = {},
    onBookSearchClick: () -> Unit = {}
) {
    val progressList by viewModel.allProgress.collectAsStateWithLifecycle()
    val allWords by viewModel.allWords.collectAsStateWithLifecycle()
    val learnedCount by viewModel.learnedCount.collectAsStateWithLifecycle()
    val dueCount by viewModel.dueWordsCount.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchLanguage by viewModel.searchLanguage.collectAsStateWithLifecycle()
    val dynamicThesaurusMap by viewModel.dynamicThesaurusMap.collectAsStateWithLifecycle()

    var localBookQuery by remember { mutableStateOf("") }
    var selectedBookFilter by remember { mutableStateOf<Int?>(null) }
    val focusManager = LocalFocusManager.current

    val books = listOf(
        Triple(1, "Essential English Words 1", "30 Units • 600 Target Words • Foundational Vocabulary"),
        Triple(2, "Essential English Words 2", "30 Units • 600 Target Words • Everyday Fluency"),
        Triple(3, "Essential English Words 3", "30 Units • 600 Target Words • Intermediate Concepts"),
        Triple(4, "Essential English Words 4", "30 Units • 600 Target Words • Academic & Professional"),
        Triple(5, "Essential English Words 5", "30 Units • 600 Target Words • Upper-Intermediate"),
        Triple(6, "Essential English Words 6", "30 Units • 600 Target Words • Advanced Mastery")
    )

    val filteredSearchResults = remember(searchResults, selectedBookFilter) {
        if (selectedBookFilter == null) {
            searchResults
        } else {
            searchResults.filter { it.bookNumber == selectedBookFilter }
        }
    }

    AdaptiveSidebarScaffold(
        activeDestination = SidebarDestination.ESSENTIAL_BOOKS,
        dueCount = dueCount,
        learnedCount = learnedCount,
        onNavigateHome = onBackClick,
        onNavigateEssentialBooks = {},
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
                    title = {
                        Column {
                            Text(
                                text = "Essential English Words All",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Books 1–6 • 180 Units • 3,600 Words",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMetaTranslucent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.testTag("essential_books_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home", tint = TextPrimaryWhite)
                            }
                            SidebarToggleIconButton(
                                isSidebarOpen = isSidebarOpen,
                                onToggleClick = onToggleSidebar
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onBookSearchClick,
                            modifier = Modifier.testTag("full_book_search_action_button")
                        ) {
                            Icon(Icons.Default.Bookmarks, contentDescription = "Book Search & Saved Filters", tint = TextPrimaryWhite)
                        }
                        if (!isWideScreen || !isSidebarOpen) {
                            IconButton(
                                onClick = onSearchClick,
                                modifier = Modifier.testTag("search_action_button")
                            ) {
                                Icon(Icons.Default.Public, contentDescription = "Global Dictionary", tint = GlassCyanAccent)
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // LAYER 2: Dedicated Local Book Search Bar (Tier 2 Glass Card)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 20.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("local_book_search_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = GlassCyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Local Book Search (3,600 Target Words)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimaryWhite
                                )
                            }

                            val searchPlaceholder = if (searchLanguage == LocalBookSearchLanguage.UZBEK) {
                                "O'zbekcha so'z qidirish..."
                            } else {
                                "Search English word..."
                            }

                            OutlinedTextField(
                                value = localBookQuery,
                                onValueChange = {
                                    localBookQuery = it
                                    viewModel.search(it, searchLanguage)
                                },
                                placeholder = {
                                    Text(
                                        searchPlaceholder,
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
                                        if (localBookQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    localBookQuery = ""
                                                    viewModel.search("", searchLanguage)
                                                },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("clear_local_book_search")
                                            ) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear local book search", tint = TextBodyTranslucent, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        val localDirectionLabel = if (searchLanguage == LocalBookSearchLanguage.UZBEK) "UZB ⇄ ENG" else "ENG ⇄ UZB"
                                        GlassPill(
                                            tintColor = if (searchLanguage == LocalBookSearchLanguage.UZBEK) GlassVioletFill else GlassCyanFill,
                                            borderColor = if (searchLanguage == LocalBookSearchLanguage.UZBEK) GlassVioletBorder else GlassCyanBorder,
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = {
                                                val nextLang = if (searchLanguage == LocalBookSearchLanguage.UZBEK) {
                                                    LocalBookSearchLanguage.ENGLISH
                                                } else {
                                                    LocalBookSearchLanguage.UZBEK
                                                }
                                                viewModel.setSearchLanguage(nextLang)
                                                if (localBookQuery.isNotBlank()) {
                                                    viewModel.search(localBookQuery, nextLang)
                                                }
                                            },
                                            modifier = Modifier
                                                .height(32.dp)
                                                .testTag("local_direction_toggle_pill")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = localDirectionLabel,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = TextPrimaryWhite
                                                )
                                            }
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    if (localBookQuery.isNotBlank()) {
                                        viewModel.search(localBookQuery, searchLanguage)
                                        viewModel.recordSearch(localBookQuery)
                                    }
                                    focusManager.clearFocus()
                                }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("local_book_search_input"),
                                shape = RoundedCornerShape(14.dp),
                                colors = glassTextFieldColors()
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedBookFilter == null,
                                    onClick = { selectedBookFilter = null },
                                    label = { Text("All Books (1–6)") },
                                    colors = glassFilterChipColors(),
                                    border = glassFilterChipBorder(selectedBookFilter == null),
                                    modifier = Modifier.testTag("local_filter_all")
                                )
                                for (bookNum in 1..6) {
                                    FilterChip(
                                        selected = selectedBookFilter == bookNum,
                                        onClick = {
                                            selectedBookFilter = if (selectedBookFilter == bookNum) null else bookNum
                                        },
                                        label = { Text("Book $bookNum") },
                                        colors = glassFilterChipColors(),
                                        border = glassFilterChipBorder(selectedBookFilter == bookNum),
                                        modifier = Modifier.testTag("local_filter_book_$bookNum")
                                    )
                                }
                            }
                        }
                    }
                }

                if (localBookQuery.isNotBlank()) {
                    if (filteredSearchResults.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GlassCard(
                                tier = GlassTier.TIER_2,
                                cornerRadius = 18.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = GlassRoseAccent
                                    )
                                    Text(
                                        text = "No textbook words found for \"${localBookQuery.trim()}\"",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryWhite,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Local Book Search only searches the 3,600 target words in Essential English Words 1–6. Use the Home Global Dictionary for general vocabulary.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextBodyTranslucent,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Found ${filteredSearchResults.size} textbook words" +
                                    (selectedBookFilter?.let { " in Book $it" } ?: " across Books 1–6"),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = GlassCyanAccent
                            )
                        }

                        items(filteredSearchResults, key = { it.id }) { wordEntity ->
                            val thesaurus = dynamicThesaurusMap[wordEntity.word.trim().lowercase(Locale.ROOT)]
                            TextbookSearchResultCard(
                                wordEntity = wordEntity,
                                searchQuery = localBookQuery,
                                searchLanguage = searchLanguage,
                                dynamicThesaurus = thesaurus,
                                onRequestThesaurus = { viewModel.ensureDynamicThesaurus(it) },
                                onWordClick = onWordClick,
                                onSpeakUk = { viewModel.speakWordWithAccent(it, isUk = true) },
                                onSpeakUs = { viewModel.speakWordWithAccent(it, isUk = false) },
                                onRelatedWordClick = { related ->
                                    localBookQuery = related
                                    viewModel.search(related, searchLanguage)
                                    focusManager.clearFocus()
                                },
                                onBookUnitClick = onUnitClick,
                                onToggleBookmark = { id, bookmarked ->
                                    viewModel.toggleBookmark(id, bookmarked)
                                }
                            )
                        }
                    }
                } else {
                    // Relocated Spaced Repetition Review Card inside Essential English Words All (right before Books 1–6)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 18.dp,
                            tintColor = GlassVioletFill,
                            borderColor = GlassVioletBorder,
                            onClick = { onSpacedRepetitionClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("essential_books_spaced_repetition_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    GlassSurfaceBox(
                                        tier = GlassTier.TIER_3,
                                        cornerRadius = 22.dp,
                                        tintColor = GlassCyanFill,
                                        borderColor = GlassCyanBorder,
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Repeat,
                                            contentDescription = null,
                                            tint = GlassCyanAccent
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Spaced Repetition Review",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimaryWhite
                                        )
                                        Text(
                                            text = if (dueCount > 0) "$dueCount words due for review today" else "All caught up! Tap to review",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextBodyTranslucent
                                        )
                                    }
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Spaced Repetition Review",
                                    tint = GlassVioletAccent
                                )
                            }
                        }
                    }

                    val visibleBooks = if (selectedBookFilter == null) {
                        books
                    } else {
                        books.filter { it.first == selectedBookFilter }
                    }

                    items(visibleBooks, key = { it.first }) { (bookNum, title, desc) ->
                        val passedUnitNums = progressList
                            .filter { it.bookNumber == bookNum && it.isPassed }
                            .map { it.unitNumber }
                            .toSet()
                        val learnedUnitNums = allWords
                            .filter { it.bookNumber == bookNum }
                            .groupBy { it.unitNumber }
                            .filter { (_, unitWords) -> unitWords.isNotEmpty() && unitWords.all { it.isLearned } }
                            .keys
                        val passedUnits = (passedUnitNums + learnedUnitNums).size.coerceIn(0, 30)
                        val progressFraction = passedUnits.toFloat() / 30f

                        val (tintFill, tintBorder, accentColor) = when (bookNum) {
                            1 -> Triple(GlassRoyalBlueFill, GlassRoyalBlueBorder, GlassRoyalBlueAccent)
                            2 -> Triple(GlassCyanFill, GlassCyanBorder, GlassCyanAccent)
                            3 -> Triple(GlassVioletFill, GlassVioletBorder, GlassVioletAccent)
                            4 -> Triple(GlassEmeraldFill, GlassEmeraldBorder, GlassEmeraldAccent)
                            5 -> Triple(GlassAmberFill, GlassAmberBorder, GlassAmberAccent)
                            else -> Triple(GlassPinkFill, GlassPinkBorder, GlassPinkAccent)
                        }

                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 20.dp,
                            tintColor = tintFill,
                            borderColor = tintBorder,
                            onClick = { onBookClick(bookNum) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("book_card_$bookNum")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            GlassPill(
                                                tintColor = tintFill,
                                                borderColor = tintBorder,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "BOOK $bookNum",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = accentColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            // Frosted-glass visual progress badge (X / 30 Units)
                                            GlassPill(
                                                tintColor = if (passedUnits > 0) GlassEmeraldFill else GlassCyanFill,
                                                borderColor = if (passedUnits > 0) GlassEmeraldBorder else GlassCyanBorder,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("book_progress_badge_$bookNum")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = if (passedUnits > 0) GlassEmeraldAccent else GlassCyanAccent,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = "$passedUnits / 30 Units",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (passedUnits > 0) GlassEmeraldAccent else TextPrimaryWhite
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimaryWhite
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextBodyTranslucent
                                        )
                                    }
                                    GlassPill(
                                        tintColor = tintFill,
                                        borderColor = tintBorder,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Open Book $bookNum Units",
                                            tint = accentColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                GlassLinearProgress(
                                    progress = progressFraction,
                                    accentColors = listOf(accentColor, GlassCyanAccent),
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 8.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Completed: $passedUnits / 30 Units",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMetaTranslucent
                                    )
                                    Text(
                                        text = "Units 1–30 ➔",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = accentColor
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
