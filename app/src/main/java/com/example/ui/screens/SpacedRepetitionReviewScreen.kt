package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.VocabularyRepository
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHairlineDivider
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.PartOfSpeechBadge
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassEmeraldAccent
import com.example.ui.theme.GlassEmeraldBorder
import com.example.ui.theme.GlassEmeraldFill
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.util.UnitTitlesHelper
import com.example.viewmodel.VocabularyViewModel

@Composable
fun SpacedRepetitionReviewScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    val allWords by viewModel.allWords.collectAsState()
    val totalWords = if (allWords.isNotEmpty()) allWords.size else 3600

    var currentIndex by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    fun moveForward() {
        if (allWords.isNotEmpty()) {
            if (currentIndex < allWords.size - 1) {
                currentIndex++
                showAnswer = false
            } else {
                isCompleted = true
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    Column {
                        Text(
                            "Vocabulary Review",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "Takrorlandi: ${currentIndex + 1} / $totalWords",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMetaTranslucent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryWhite)
                    }
                },
                actions = {
                    GlassPill(
                        tintColor = GlassVioletFill,
                        borderColor = GlassVioletBorder,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Reviewed: ${currentIndex + 1} of $totalWords",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (allWords.isEmpty()) {
                CircularProgressIndicator(color = GlassCyanAccent)
            } else if (isCompleted) {
                GlassCard(
                    tier = GlassTier.TIER_2,
                    cornerRadius = 24.dp,
                    tintColor = GlassEmeraldFill,
                    borderColor = GlassEmeraldBorder,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GlassSurfaceBox(
                            tier = GlassTier.TIER_3,
                            cornerRadius = 36.dp,
                            tintColor = GlassEmeraldFill,
                            borderColor = GlassEmeraldBorder,
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(72.dp).clip(CircleShape)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GlassEmeraldAccent,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Text(
                            text = "Barcha $totalWords so'z takrorlandi! 🏆",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Siz barcha 6 kitobdagi $totalWords ta so'z bo'yicha takrorlash mashg'ulotini muvaffaqiyatli yakunladingiz.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = TextBodyTranslucent
                        )
                        GlassActionButton(
                            onClick = {
                                currentIndex = 0
                                isCompleted = false
                                showAnswer = false
                            },
                            tintColor = GlassCyanFill,
                            borderColor = GlassCyanBorder,
                            cornerRadius = 14.dp,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Qaytadan boshlash (1-so'zdan)", fontWeight = FontWeight.Bold)
                        }
                        GlassActionButton(
                            onClick = onBackClick,
                            tintColor = GlassTier3Fill,
                            borderColor = GlassTier3Border,
                            cornerRadius = 14.dp,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Asosiy menyuga qaytish")
                        }
                    }
                }
            } else {
                val currentWord = allWords[currentIndex.coerceIn(0, allWords.lastIndex)]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassPill(
                                tintColor = GlassCyanFill,
                                borderColor = GlassCyanBorder,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Book ${currentWord.bookNumber} • Unit ${currentWord.unitNumber}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassCyanAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = "Takrorlandi: ${currentIndex + 1} / $totalWords",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GlassCyanAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        GlassLinearProgress(
                            progress = ((currentIndex + 1).toFloat() / totalWords).coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth(),
                            height = 8.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = UnitTitlesHelper.getTitle(currentWord.bookNumber, currentWord.unitNumber),
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = TextMetaTranslucent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedContent(
                        targetState = currentIndex,
                        transitionSpec = {
                            slideInHorizontally(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) { fullWidth -> fullWidth } + fadeIn() togetherWith
                                slideOutHorizontally(
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                ) { fullWidth -> -fullWidth } + fadeOut()
                        },
                        label = "cardForwardSwipeAnimation",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { targetIdx ->
                        val wordItem = allWords[targetIdx.coerceIn(0, allWords.lastIndex)]

                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 26.dp,
                            tintColor = if (showAnswer) GlassVioletFill else GlassCyanFill,
                            borderColor = if (showAnswer) GlassVioletBorder else GlassCyanBorder,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    if (!showAnswer) {
                                        showAnswer = true
                                    } else {
                                        viewModel.reviewWord(wordItem.id, VocabularyRepository.ReviewGrade.GOOD)
                                        moveForward()
                                    }
                                }
                                .testTag("review_word_card_${wordItem.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                DualAudioButtons(
                                    word = wordItem.word,
                                    onSpeakUs = { viewModel.speakWordWithAccent(wordItem.word, false) },
                                    onSpeakUk = { viewModel.speakWordWithAccent(wordItem.word, true) }
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = wordItem.word,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 34.sp
                                    ),
                                    color = TextPrimaryWhite,
                                    textAlign = TextAlign.Center
                                )

                                if (wordItem.partOfSpeech.isNotBlank() || wordItem.phonetic.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PartOfSpeechBadge(
                                            partOfSpeech = wordItem.partOfSpeech,
                                            tagSuffix = "review_${wordItem.id}"
                                        )
                                        if (wordItem.phonetic.isNotBlank()) {
                                            Text(
                                                text = wordItem.phonetic,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                                color = TextMetaTranslucent
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                GlassHairlineDivider(color = GlassTier1Border)
                                Spacer(modifier = Modifier.height(20.dp))

                                if (showAnswer) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (wordItem.uzbekTranslation.isNotBlank()) {
                                            GlassPill(
                                                tintColor = GlassEmeraldFill,
                                                borderColor = GlassEmeraldBorder,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "🇺🇿 ${wordItem.uzbekTranslation}",
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = GlassEmeraldAccent,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                                )
                                            }
                                        }

                                        val def = if (wordItem.definition.isNotBlank()) wordItem.definition else wordItem.englishDefinition
                                        if (def.isNotBlank()) {
                                            Text(
                                                text = def,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center,
                                                color = TextBodyTranslucent
                                            )
                                        }

                                        val ex = if (wordItem.exampleSentenceEn.isNotBlank()) wordItem.exampleSentenceEn else wordItem.exampleSentence
                                        if (ex.isNotBlank()) {
                                            Text(
                                                text = "“$ex”",
                                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                                textAlign = TextAlign.Center,
                                                color = TextMetaTranslucent
                                            )
                                        }

                                        Text(
                                            text = "Oldinga o'tish uchun kartani bosing yoki tugmalardan birini tanlang",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GlassCyanAccent,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    GlassActionButton(
                                        onClick = { showAnswer = true },
                                        tintColor = GlassTier3Fill,
                                        borderColor = GlassTier3Border,
                                        cornerRadius = 14.dp,
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp), tint = GlassCyanAccent)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Javobni ko'rish (Show Answer)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (showAnswer) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassActionButton(
                                    onClick = {
                                        viewModel.reviewWord(currentWord.id, VocabularyRepository.ReviewGrade.AGAIN)
                                        moveForward()
                                    },
                                    tintColor = GlassRoseFill,
                                    borderColor = GlassRoseBorder,
                                    cornerRadius = 12.dp,
                                    contentPadding = PaddingValues(6.dp),
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Again", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("1 kun", fontSize = 10.sp, color = TextBodyTranslucent)
                                    }
                                }

                                GlassActionButton(
                                    onClick = {
                                        viewModel.reviewWord(currentWord.id, VocabularyRepository.ReviewGrade.GOOD)
                                        moveForward()
                                    },
                                    tintColor = GlassRoyalBlueFill,
                                    borderColor = GlassRoyalBlueBorder,
                                    cornerRadius = 12.dp,
                                    contentPadding = PaddingValues(6.dp),
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Good", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("3 kun", fontSize = 10.sp, color = TextBodyTranslucent)
                                    }
                                }

                                GlassActionButton(
                                    onClick = {
                                        viewModel.reviewWord(currentWord.id, VocabularyRepository.ReviewGrade.EASY)
                                        moveForward()
                                    },
                                    tintColor = GlassEmeraldFill,
                                    borderColor = GlassEmeraldBorder,
                                    cornerRadius = 12.dp,
                                    contentPadding = PaddingValues(6.dp),
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Easy", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("7 kun", fontSize = 10.sp, color = TextBodyTranslucent)
                                    }
                                }
                            }

                            GlassActionButton(
                                onClick = {
                                    viewModel.reviewWord(currentWord.id, VocabularyRepository.ReviewGrade.GOOD)
                                    moveForward()
                                },
                                tintColor = GlassCyanFill,
                                borderColor = GlassCyanBorder,
                                cornerRadius = 14.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("next_known_button")
                            ) {
                                Text(
                                    text = "Keyingisi / Bilingan (Next / Known) →",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (currentIndex > 0) {
                                GlassActionButton(
                                    onClick = {
                                        currentIndex--
                                        showAnswer = false
                                    },
                                    tintColor = GlassTier3Fill,
                                    borderColor = GlassTier3Border,
                                    cornerRadius = 14.dp,
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(0.35f).height(52.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Orqaga", fontSize = 12.sp)
                                }
                            }

                            GlassActionButton(
                                onClick = { showAnswer = true },
                                tintColor = GlassCyanFill,
                                borderColor = GlassCyanBorder,
                                cornerRadius = 14.dp,
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("flip_card_button")
                            ) {
                                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Flip Card", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            GlassActionButton(
                                onClick = {
                                    viewModel.reviewWord(currentWord.id, VocabularyRepository.ReviewGrade.GOOD)
                                    moveForward()
                                },
                                tintColor = GlassEmeraldFill,
                                borderColor = GlassEmeraldBorder,
                                cornerRadius = 14.dp,
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .weight(0.65f)
                                    .height(52.dp)
                                    .testTag("quick_next_button")
                            ) {
                                Text("Bilingan ✓", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
