package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WordEntity
import com.example.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    wordId: Long,
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    var wordEntity by remember { mutableStateOf<WordEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val isUkAccent by viewModel.isUkAccent.collectAsState()

    LaunchedEffect(wordId) {
        isLoading = true
        wordEntity = viewModel.getWordById(wordId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(wordEntity?.word ?: "Word Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    wordEntity?.let { word ->
                        IconButton(onClick = { viewModel.toggleBookmark(word.id, !word.isBookmarked) }) {
                            Icon(
                                imageVector = if (word.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (word.isBookmarked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
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
                CircularProgressIndicator()
            } else if (wordEntity == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Word not found", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) { Text("Go Back") }
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
                    // Word Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (word.phonetic.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = word.phonetic,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            if (word.uzbekTranslation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🇺🇿 ${word.uzbekTranslation}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0D9488)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // TTS Audio Play Button
                            FilledTonalIconButton(
                                onClick = { viewModel.speakWord(word.word) },
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag("audio_play_button")
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Pronounce Word",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = if (isUkAccent) "Accent: 🇬🇧 British English" else "Accent: 🇺🇸 American English",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // English & Uzbek Definitions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Definitions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val engDef = word.englishDefinition.ifBlank { word.definition }
                            if (engDef.isNotBlank()) {
                                Text(
                                    text = "EN: $engDef",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            if (word.definition.isNotBlank() && word.definition != engDef) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "UZ: ${word.definition}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Examples
                    if (word.exampleSentenceEn.isNotBlank() || word.exampleSentenceUz.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Example Sentences",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                if (word.exampleSentenceEn.isNotBlank()) {
                                    Text(
                                        text = "“${word.exampleSentenceEn}”",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                                if (word.exampleSentenceUz.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🇺🇿 ${word.exampleSentenceUz}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Synonyms and Antonyms
                    if (word.synonyms.isNotBlank() || word.antonyms.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                if (word.synonyms.isNotBlank()) {
                                    Text(
                                        "Synonyms: ${word.synonyms}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFF047857)
                                    )
                                }
                                if (word.antonyms.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Antonyms: ${word.antonyms}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mark as learned button
                    Button(
                        onClick = { viewModel.toggleLearned(word.id, !word.isLearned) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (word.isLearned) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (word.isLearned) "Marked as Learned ✓" else "Mark as Learned")
                    }
                }
            }
        }
    }
}
