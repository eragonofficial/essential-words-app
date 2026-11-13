package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.PartOfSpeechBadge
import com.example.ui.theme.GlassAmberAccent
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavedWordsScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    val bookmarkedWords by viewModel.bookmarkedWords.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = { Text("Saved Words", color = TextPrimaryWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryWhite)
                    }
                }
            )
        }
    ) { padding ->
        if (bookmarkedWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(tier = GlassTier.TIER_2, cornerRadius = 22.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = GlassAmberAccent,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Saved Words Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "Bookmark words inside any Unit to access them quickly here.",
                            style = MaterialTheme.typography.bodySmall,
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
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookmarkedWords, key = { it.id }) { wordEntity ->
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 18.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FlowRow(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = wordEntity.word,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryWhite,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    PartOfSpeechBadge(
                                        partOfSpeech = wordEntity.partOfSpeech,
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        tagSuffix = "saved_${wordEntity.id}"
                                    )
                                    if (wordEntity.phonetic.isNotBlank()) {
                                        Text(
                                            text = wordEntity.phonetic,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMetaTranslucent,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = wordEntity.definition,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextBodyTranslucent
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                DualAudioButtons(
                                    word = wordEntity.word,
                                    onSpeakUs = { viewModel.speakWordWithAccent(wordEntity.word, false) },
                                    onSpeakUk = { viewModel.speakWordWithAccent(wordEntity.word, true) }
                                )
                                IconButton(onClick = { viewModel.toggleBookmark(wordEntity.id, false) }) {
                                    Icon(Icons.Default.Bookmark, contentDescription = "Remove", tint = GlassAmberAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
