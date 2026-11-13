package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WordEntity
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHairlineDivider
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.components.PartOfSpeechBadge
import com.example.ui.components.glassFilterChipBorder
import com.example.ui.components.glassFilterChipColors
import com.example.ui.components.glassTextFieldColors
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
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassVioletAccent
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordListFlashcardMode(
    words: List<WordEntity>,
    flashcardIndex: Int,
    isFlipped: Boolean,
    isUkAccent: Boolean,
    onFlipToggle: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSpeakWordWithAccent: (String, Boolean) -> Unit,
    onToggleLearned: (Long, Boolean) -> Unit
) {
    val currentWord = words[flashcardIndex.coerceIn(0, words.lastIndex)]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Card ${flashcardIndex + 1} of ${words.size}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = GlassCyanAccent
            )
            FilterChip(
                selected = currentWord.isLearned,
                onClick = { onToggleLearned(currentWord.id, !currentWord.isLearned) },
                label = { Text(if (currentWord.isLearned) "Learned ✓" else "Mark Learned") },
                colors = glassFilterChipColors(
                    selectedFill = GlassEmeraldFill,
                    selectedBorder = GlassEmeraldBorder
                ),
                border = glassFilterChipBorder(
                    selected = currentWord.isLearned,
                    selectedBorder = GlassEmeraldBorder
                )
            )
        }

        GlassLinearProgress(
            progress = (flashcardIndex + 1).toFloat() / words.size,
            modifier = Modifier.fillMaxWidth(),
            height = 6.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Multi-layered Frosted Glass Flashcard (Both Front & Back)
        GlassCard(
            tier = GlassTier.TIER_2,
            cornerRadius = 26.dp,
            tintColor = if (isFlipped) GlassVioletFill else GlassCyanFill,
            borderColor = if (isFlipped) GlassVioletBorder else GlassCyanBorder,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { onFlipToggle() }
                .testTag("active_recall_flashcard")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = isFlipped,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "flashcardFlipAnimation"
                ) { flipped ->
                    if (!flipped) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DualAudioButtons(
                                word = currentWord.word,
                                onSpeakUs = { onSpeakWordWithAccent(currentWord.word, false) },
                                onSpeakUk = { onSpeakWordWithAccent(currentWord.word, true) }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = currentWord.word,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextPrimaryWhite,
                                textAlign = TextAlign.Center
                            )

                            if (currentWord.partOfSpeech.isNotBlank() || currentWord.phonetic.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PartOfSpeechBadge(
                                        partOfSpeech = currentWord.partOfSpeech,
                                        tagSuffix = "flashcard_front_${currentWord.id}"
                                    )
                                    if (currentWord.phonetic.isNotBlank()) {
                                        GlassPill(
                                            tintColor = GlassTier3Fill,
                                            borderColor = GlassTier3Border,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = currentWord.phonetic,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextMetaTranslucent,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(36.dp))

                            GlassPill(
                                tintColor = GlassTier3Fill,
                                borderColor = GlassTier3Border,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = GlassCyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Tap card to flip definition & translation",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = TextBodyTranslucent
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = currentWord.word,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryWhite,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    PartOfSpeechBadge(
                                        partOfSpeech = currentWord.partOfSpeech,
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        tagSuffix = "flashcard_back_${currentWord.id}"
                                    )
                                    if (currentWord.phonetic.isNotBlank()) {
                                        GlassPill(
                                            tintColor = GlassTier3Fill,
                                            borderColor = GlassTier3Border,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        ) {
                                            Text(
                                                text = currentWord.phonetic,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMetaTranslucent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                DualAudioButtons(
                                    word = currentWord.word,
                                    onSpeakUs = { onSpeakWordWithAccent(currentWord.word, false) },
                                    onSpeakUk = { onSpeakWordWithAccent(currentWord.word, true) }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (currentWord.uzbekTranslation.isNotBlank()) {
                                GlassPill(
                                    tintColor = GlassEmeraldFill,
                                    borderColor = GlassEmeraldBorder,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "🇺🇿 ${currentWord.uzbekTranslation}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GlassEmeraldAccent,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            val def = if (currentWord.definition.isNotBlank()) currentWord.definition else currentWord.englishDefinition
                            if (def.isNotBlank()) {
                                Text(
                                    "Definition:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassCyanAccent
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(def, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryWhite)
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (currentWord.uzbekDefinition.isNotBlank()) {
                                Text(
                                    "🇺🇿 O'zbekcha izohi:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassEmeraldAccent
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    currentWord.uzbekDefinition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextBodyTranslucent
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            val exEn = if (currentWord.exampleSentenceEn.isNotBlank()) currentWord.exampleSentenceEn else currentWord.exampleSentence
                            if (exEn.isNotBlank()) {
                                Text(
                                    "Example:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassVioletAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                GlassSurfaceBox(
                                    tier = GlassTier.TIER_3,
                                    cornerRadius = 12.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            "“$exEn”",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                            color = TextPrimaryWhite
                                        )
                                        if (currentWord.exampleSentenceUz.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                "“${currentWord.exampleSentenceUz}”",
                                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                                color = TextMetaTranslucent
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            GlassHairlineDivider(color = GlassTier1Border)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap anywhere to flip back 🔄",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMetaTranslucent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassActionButton(
                onClick = onPrev,
                enabled = flashcardIndex > 0,
                tintColor = GlassTier3Fill,
                borderColor = GlassTier3Border,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous", fontWeight = FontWeight.SemiBold)
            }

            GlassActionButton(
                onClick = onFlipToggle,
                tintColor = GlassVioletFill,
                borderColor = GlassVioletBorder,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp), tint = GlassVioletAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isFlipped) "Show Word" else "Flip Card", fontWeight = FontWeight.Bold)
            }

            GlassActionButton(
                onClick = onNext,
                enabled = flashcardIndex < words.size - 1,
                tintColor = GlassCyanFill,
                borderColor = GlassCyanBorder,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("Next", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun WordListTypingQuizMode(
    words: List<WordEntity>,
    typingIndex: Int,
    typingInput: String,
    typingFeedback: String?,
    isTypingCorrect: Boolean,
    typingScore: Int,
    typingFinished: Boolean,
    isCheckingTyping: Boolean,
    onInputChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onSpeakWordWithAccent: (String, Boolean) -> Unit,
    onPracticeAgain: () -> Unit,
    onReturnToList: () -> Unit
) {
    if (typingFinished) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(
                tier = GlassTier.TIER_2,
                cornerRadius = 24.dp,
                tintColor = GlassEmeraldFill,
                borderColor = GlassEmeraldBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Typing Quiz Finished! 🏆",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryWhite,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Score: $typingScore of ${words.size} correct",
                        style = MaterialTheme.typography.titleLarge,
                        color = GlassEmeraldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    GlassActionButton(
                        onClick = onPracticeAgain,
                        tintColor = GlassCyanFill,
                        borderColor = GlassCyanBorder,
                        cornerRadius = 14.dp,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Practice Again", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onReturnToList) {
                        Text("Return to Word List", color = GlassCyanAccent)
                    }
                }
            }
        }
    } else {
        val currentWord = words[typingIndex.coerceIn(0, words.lastIndex)]
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassLinearProgress(
                progress = (typingIndex + 1).toFloat() / words.size,
                modifier = Modifier.fillMaxWidth(),
                height = 8.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Word ${typingIndex + 1} of ${words.size}",
                style = MaterialTheme.typography.labelMedium,
                color = TextMetaTranslucent
            )

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(
                tier = GlassTier.TIER_2,
                cornerRadius = 22.dp,
                tintColor = GlassRoyalBlueFill,
                borderColor = GlassRoyalBlueBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Translate this word to Uzbek:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextBodyTranslucent
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentWord.word,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimaryWhite
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        DualAudioButtons(
                            word = currentWord.word,
                            onSpeakUs = { onSpeakWordWithAccent(currentWord.word, false) },
                            onSpeakUk = { onSpeakWordWithAccent(currentWord.word, true) }
                        )
                    }

                    if (currentWord.partOfSpeech.isNotBlank() || currentWord.phonetic.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PartOfSpeechBadge(
                                partOfSpeech = currentWord.partOfSpeech,
                                tagSuffix = "typing_${currentWord.id}"
                            )
                            if (currentWord.phonetic.isNotBlank()) {
                                Text(
                                    text = currentWord.phonetic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMetaTranslucent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = typingInput,
                onValueChange = onInputChange,
                label = { Text("Enter Uzbek translation") },
                placeholder = { Text("e.g. ${currentWord.uzbekTranslation.take(2)}...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("typing_input"),
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmitAnswer() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassActionButton(
                onClick = onSubmitAnswer,
                enabled = !isCheckingTyping && typingInput.isNotBlank(),
                tintColor = GlassCyanFill,
                borderColor = GlassCyanBorder,
                cornerRadius = 14.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_translation_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Check Answer", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            typingFeedback?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isTypingCorrect) GlassEmeraldFill else GlassRoseFill,
                    border = BorderStroke(1.dp, if (isTypingCorrect) GlassEmeraldBorder else GlassRoseBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isTypingCorrect) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isTypingCorrect) GlassEmeraldAccent else GlassRoseAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = if (isTypingCorrect) GlassEmeraldAccent else GlassRoseAccent,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
