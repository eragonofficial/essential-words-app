package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.WordEntity
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.PartOfSpeechBadge
import com.example.ui.components.mergeThesaurusTerms
import com.example.ui.theme.GlassAmberAccent
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassEmeraldAccent
import com.example.ui.theme.GlassEmeraldBorder
import com.example.ui.theme.GlassEmeraldFill
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel
import java.util.Locale

@Composable
fun WordDetailScreen(
    wordId: Long,
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    var wordEntity by remember { mutableStateOf<WordEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val isUkAccent by viewModel.isUkAccent.collectAsStateWithLifecycle()
    val dynamicThesaurusMap by viewModel.dynamicThesaurusMap.collectAsStateWithLifecycle()

    LaunchedEffect(wordId) {
        isLoading = true
        val loaded = viewModel.getWordById(wordId)
        wordEntity = loaded
        if (loaded != null) {
            viewModel.ensureDynamicThesaurus(loaded.word)
        }
        isLoading = false
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    Text(
                        wordEntity?.word ?: "Word Detail",
                        color = TextPrimaryWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryWhite)
                    }
                },
                actions = {
                    wordEntity?.let { word ->
                        IconButton(
                            onClick = {
                                val nextBookmarked = !word.isBookmarked
                                viewModel.toggleBookmark(word.id, nextBookmarked)
                                wordEntity = word.copy(isBookmarked = nextBookmarked)
                            },
                            modifier = Modifier.testTag("word_detail_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (word.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (word.isBookmarked) GlassAmberAccent else TextPrimaryWhite
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = GlassCyanAccent)
            } else if (wordEntity == null) {
                GlassCard(
                    tier = GlassTier.TIER_2,
                    cornerRadius = 22.dp,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Word not found", style = MaterialTheme.typography.titleMedium, color = TextPrimaryWhite)
                        Spacer(modifier = Modifier.height(16.dp))
                        GlassActionButton(onClick = onBackClick) {
                            Text("Go Back", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                val word = wordEntity!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Word Hero Card (Tier 2 Glass)
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 24.dp,
                        tintColor = GlassCyanFill,
                        borderColor = GlassCyanBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextPrimaryWhite
                            )

                            if (word.partOfSpeech.isNotBlank() || word.phonetic.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PartOfSpeechBadge(
                                        partOfSpeech = word.partOfSpeech,
                                        tagSuffix = "detail_${word.id}"
                                    )
                                    if (word.phonetic.isNotBlank()) {
                                        GlassPill(
                                            tintColor = GlassTier3Fill,
                                            borderColor = GlassTier3Border,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = word.phonetic,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextMetaTranslucent,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (word.uzbekTranslation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                GlassPill(
                                    tintColor = GlassEmeraldFill,
                                    borderColor = GlassEmeraldBorder,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "🇺🇿 ${word.uzbekTranslation}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = GlassEmeraldAccent,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            DualAudioButtons(
                                word = word.word,
                                onSpeakUs = { viewModel.speakWordWithAccent(word.word, false) },
                                onSpeakUk = { viewModel.speakWordWithAccent(word.word, true) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // English & Uzbek Definitions
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 20.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Definitions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GlassCyanAccent
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val engDef = word.englishDefinition.ifBlank { word.definition }
                            if (engDef.isNotBlank()) {
                                Text(
                                    text = "EN: $engDef",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimaryWhite
                                )
                            }
                            val uzDef = word.uzbekDefinition.ifBlank { if (word.definition != engDef) word.definition else "" }
                            if (uzDef.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🇺🇿 O'zbekcha izohi: $uzDef",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextBodyTranslucent
                                )
                            }
                        }
                    }

                    // Examples
                    if (word.exampleSentenceEn.isNotBlank() || word.exampleSentenceUz.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 20.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Example Sentences",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassCyanAccent
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                if (word.exampleSentenceEn.isNotBlank()) {
                                    Text(
                                        text = "“${word.exampleSentenceEn}”",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = FontStyle.Italic,
                                        color = TextPrimaryWhite
                                    )
                                }
                                if (word.exampleSentenceUz.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🇺🇿 Misol tarjimasi: “${word.exampleSentenceUz}”",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextBodyTranslucent
                                    )
                                }
                            }
                        }
                    }

                    // Synonyms and Antonyms
                    val thesaurus = dynamicThesaurusMap[word.word.trim().lowercase(Locale.ROOT)]
                    val mergedSynonyms = mergeThesaurusTerms(word.synonyms, thesaurus?.synonyms.orEmpty(), word.word)
                    val mergedAntonyms = mergeThesaurusTerms(word.antonyms, thesaurus?.antonyms.orEmpty(), word.word)
                    if (mergedSynonyms.isNotEmpty() || mergedAntonyms.isNotEmpty() || thesaurus?.isLoading == true) {
                        Spacer(modifier = Modifier.height(14.dp))
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 20.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (thesaurus?.isLoading == true && mergedSynonyms.isEmpty() && mergedAntonyms.isEmpty()) {
                                    GlassLinearProgress(progress = 0.65f, modifier = Modifier.fillMaxWidth(), height = 4.dp)
                                }
                                if (mergedSynonyms.isNotEmpty()) {
                                    Text(
                                        "Synonyms (Sinonimlar): ${mergedSynonyms.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = GlassEmeraldAccent
                                    )
                                }
                                if (mergedAntonyms.isNotEmpty()) {
                                    Text(
                                        "Antonyms (Antonimlar): ${mergedAntonyms.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = GlassRoseAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    GlassActionButton(
                        onClick = { viewModel.toggleLearned(word.id, !word.isLearned) },
                        tintColor = if (word.isLearned) GlassEmeraldFill else GlassCyanFill,
                        borderColor = if (word.isLearned) GlassEmeraldBorder else GlassCyanBorder,
                        cornerRadius = 14.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (word.isLearned) GlassEmeraldAccent else GlassCyanAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (word.isLearned) "Marked as Learned ✓" else "Mark as Learned",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
