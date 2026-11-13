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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CompareArrows
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
import androidx.compose.ui.unit.dp
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.PartOfSpeechBadge
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassEmeraldAccent
import com.example.ui.theme.GlassEmeraldBorder
import com.example.ui.theme.GlassEmeraldFill
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SynonymsAntonymsScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    val wordList by viewModel.synonymsAndAntonyms.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    Text(
                        "Synonyms & Antonyms",
                        color = TextPrimaryWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryWhite)
                    }
                }
            )
        }
    ) { padding ->
        if (wordList.isEmpty()) {
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
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = GlassCyanAccent, modifier = Modifier.size(48.dp))
                        Text("No Synonyms & Antonyms Listed Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimaryWhite)
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
                items(wordList, key = { it.id }) { wordEntity ->
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 18.dp,
                        modifier = Modifier.fillMaxWidth()
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
                                FlowRow(
                                    modifier = Modifier.weight(1f),
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
                                        tagSuffix = "syn_${wordEntity.id}"
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
                                DualAudioButtons(
                                    word = wordEntity.word,
                                    onSpeakUs = { viewModel.speakWordWithAccent(wordEntity.word, false) },
                                    onSpeakUk = { viewModel.speakWordWithAccent(wordEntity.word, true) }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = wordEntity.definition,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextBodyTranslucent
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            if (wordEntity.synonyms.isNotEmpty()) {
                                GlassPill(
                                    tintColor = GlassEmeraldFill,
                                    borderColor = GlassEmeraldBorder,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Synonyms: ${wordEntity.synonyms}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassEmeraldAccent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            if (wordEntity.antonyms.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                GlassPill(
                                    tintColor = GlassRoseFill,
                                    borderColor = GlassRoseBorder,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Antonyms: ${wordEntity.antonyms}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassRoseAccent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
