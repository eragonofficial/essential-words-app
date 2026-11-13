package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.dictionary.SearchDirection
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHairlineDivider
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.GlobalDictionaryEntryCard
import com.example.ui.components.glassFilterChipBorder
import com.example.ui.components.glassFilterChipColors
import com.example.ui.components.glassTextFieldColors
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.GlobalSearchViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: GlobalSearchViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val sampleQueries = if (uiState.direction == SearchDirection.ENG_TO_UZB) {
        listOf("serendipity", "ubiquitous", "algorithm", "ephemeral", "resilient", "sustainability")
    } else {
        listOf("kitob", "do'st", "muvaffaqiyat", "kelajak", "bilim", "harakat")
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = {
                            Text(
                                text = uiState.direction.hint,
                                maxLines = 1,
                                color = TextMetaTranslucent
                            )
                        },
                        singleLine = true,
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.query.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.onQueryChange("") },
                                        modifier = Modifier.testTag("clear_global_search_button")
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextBodyTranslucent)
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.submitSearch()
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier.testTag("submit_global_search_button")
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search global dictionary", tint = GlassCyanAccent)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.submitSearch()
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp)
                            .testTag("global_search_input"),
                        colors = glassTextFieldColors(),
                        shape = RoundedCornerShape(14.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("global_search_back_button")
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
            // Bidirectional Mode Toggle Strip (Tier 1 Glass)
            GlassSurfaceBox(
                tier = GlassTier.TIER_1,
                cornerRadius = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.direction == SearchDirection.ENG_TO_UZB,
                        onClick = { viewModel.setDirection(SearchDirection.ENG_TO_UZB) },
                        label = {
                            Text(
                                text = SearchDirection.ENG_TO_UZB.label,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = glassFilterChipColors(),
                        border = glassFilterChipBorder(uiState.direction == SearchDirection.ENG_TO_UZB),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_eng_uzb")
                    )

                    GlassPill(
                        shape = CircleShape,
                        tintColor = GlassTier3Fill,
                        borderColor = GlassTier3Border,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("swap_direction_button"),
                        onClick = { viewModel.swapDirection() }
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Swap translation direction",
                            tint = GlassCyanAccent
                        )
                    }

                    FilterChip(
                        selected = uiState.direction == SearchDirection.UZB_TO_ENG,
                        onClick = { viewModel.setDirection(SearchDirection.UZB_TO_ENG) },
                        label = {
                            Text(
                                text = SearchDirection.UZB_TO_ENG.label,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = glassFilterChipColors(
                            selectedFill = GlassVioletFill,
                            selectedBorder = GlassVioletBorder
                        ),
                        border = glassFilterChipBorder(
                            selected = uiState.direction == SearchDirection.UZB_TO_ENG,
                            selectedBorder = GlassVioletBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_uzb_eng")
                    )
                }
            }

            if (uiState.isLoading) {
                GlassLinearProgress(
                    progress = 0.7f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("global_search_loading"),
                    height = 4.dp
                )
            }

            // Recent Searches (last 5 successful dictionary lookups)
            if (uiState.recentSearches.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("global_recent_lookups_section")
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
                                "Recent Lookups (${uiState.recentSearches.take(5).size}/5)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryWhite
                            )
                        }
                        TextButton(
                            onClick = { viewModel.clearRecentSearches() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("clear_recent_global_searches")
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
                        uiState.recentSearches.take(5).forEach { recent ->
                            val isSelected = recent.equals(uiState.query.trim(), ignoreCase = true)
                            Surface(
                                onClick = {
                                    viewModel.revisitRecentSearch(recent)
                                    focusManager.clearFocus()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) GlassCyanFill else GlassTier3Fill,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) GlassCyanBorder else GlassTier3Border
                                ),
                                modifier = Modifier.testTag("global_recent_lookup_$recent")
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
                                            .size(16.dp)
                                            .clickable { viewModel.deleteRecentSearch(recent) }
                                    )
                                }
                            }
                        }
                    }
                    GlassHairlineDivider(modifier = Modifier.padding(top = 8.dp), color = GlassTier1Border)
                }
            }

            when {
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 22.dp,
                            tintColor = GlassRoseFill,
                            borderColor = GlassRoseBorder,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp),
                                    tint = GlassRoseAccent
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = uiState.errorMessage ?: "Lookup error",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimaryWhite,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                GlassActionButton(
                                    onClick = { viewModel.submitSearch() },
                                    tintColor = GlassRoseFill,
                                    borderColor = GlassRoseBorder,
                                    modifier = Modifier.testTag("retry_global_search_button")
                                ) {
                                    Text("Retry", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                uiState.query.isBlank() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GlassCard(
                                tier = GlassTier.TIER_2,
                                cornerRadius = 22.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                            Icons.Default.Public,
                                            contentDescription = null,
                                            modifier = Modifier.size(38.dp),
                                            tint = GlassCyanAccent
                                        )
                                    }
                                    Text(
                                        text = "Global English ↔ Uzbek Dictionary",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryWhite,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (uiState.direction == SearchDirection.ENG_TO_UZB) {
                                            "Look up any English word in the world for genuine definitions, IPA phonetics, audio pronunciation, Uzbek translations, and synonyms/antonyms."
                                        } else {
                                            "Enter any Uzbek word to resolve its English equivalents along with phonetics, audio, English definitions, synonyms, and antonyms."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextBodyTranslucent,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        item {
                            Text(
                                text = "Try looking up:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = GlassCyanAccent
                            )
                        }
                        item {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                sampleQueries.forEach { sample ->
                                    Surface(
                                        onClick = {
                                            viewModel.submitSearch(sample)
                                            focusManager.clearFocus()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = GlassTier3Fill,
                                        border = BorderStroke(1.dp, GlassTier3Border),
                                        modifier = Modifier.testTag("sample_query_$sample")
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

                !uiState.isLoading && uiState.hasSearched && uiState.entries.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            GlassCard(
                                tier = GlassTier.TIER_2,
                                cornerRadius = 22.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(52.dp),
                                        tint = GlassRoseAccent
                                    )
                                    Text(
                                        text = "No dictionary entry found for \"${uiState.query.trim()}\"",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryWhite,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (uiState.direction == SearchDirection.ENG_TO_UZB) {
                                            "Check spelling or switch to UZB ➔ ENG if you entered an Uzbek word."
                                        } else {
                                            "Try another Uzbek word or switch to ENG ➔ UZB if you entered an English word."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextBodyTranslucent,
                                        textAlign = TextAlign.Center
                                    )
                                    if (uiState.suggestions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Did you mean:",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = GlassCyanAccent
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            uiState.suggestions.forEach { suggestion ->
                                                Surface(
                                                    onClick = {
                                                        viewModel.selectSuggestion(suggestion)
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
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.suggestions.isNotEmpty()) {
                            item {
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
                                        uiState.suggestions.forEach { suggestion ->
                                            Surface(
                                                onClick = {
                                                    viewModel.selectSuggestion(suggestion)
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
                            uiState.entries,
                            key = { "${it.tier}_${it.direction}_${it.headword}_${it.matchedFromUzbek ?: ""}_${it.geoInfo?.entityType ?: ""}" }
                        ) { entry ->
                            GlobalDictionaryEntryCard(
                                entry = entry,
                                onPronounceUs = {
                                    viewModel.pronounceUs(entry.headword, entry.audioUrlUs)
                                },
                                onPronounceUk = {
                                    viewModel.pronounceUk(entry.headword, entry.audioUrlUk)
                                },
                                onRelatedWordClick = { word ->
                                    viewModel.lookupEnglishWord(word)
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
