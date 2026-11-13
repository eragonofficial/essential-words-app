package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.PartOfSpeechBadge
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
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VocabularyPracticeScreen(
    bookNumber: Int,
    unitNumber: Int,
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    val words by viewModel.currentUnitWords.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { toneGenerator?.release() } catch (_: Exception) {}
        }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(bookNumber, unitNumber) {
        viewModel.loadWordsForUnit(bookNumber, unitNumber)
    }

    fun submitAnswer() {
        if (isSubmitting || words.isEmpty() || currentIndex !in words.indices) return
        val currentWord = words[currentIndex]
        val cleanInput = userInput.trim()
        val cleanTarget = currentWord.uzbekTranslation.trim()

        isSubmitting = true
        val matched = cleanInput.equals(cleanTarget, ignoreCase = true) ||
            (cleanTarget.contains(cleanInput, ignoreCase = true) && cleanInput.length >= 3)

        if (matched) {
            isCorrect = true
            feedback = "Correct! 🎉"
            score++
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200) } catch (_: Exception) {}
        } else {
            isCorrect = false
            feedback = "Incorrect. Correct: ${currentWord.uzbekTranslation}"
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250) } catch (_: Exception) {}
        }

        coroutineScope.launch {
            delay(1500)
            if (currentIndex < words.size - 1) {
                currentIndex++
                userInput = ""
                feedback = null
                isSubmitting = false
            } else {
                quizFinished = true
                isSubmitting = false
                viewModel.recordQuizScore("Typing Quiz", bookNumber, unitNumber, score, words.size)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    Text(
                        "Typing Practice: B$bookNumber • U$unitNumber",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (words.isEmpty()) {
                CircularProgressIndicator(color = GlassCyanAccent)
            } else if (quizFinished) {
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
                            text = "Practice Completed! 🏆",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Score: $score of ${words.size} words",
                            style = MaterialTheme.typography.titleLarge,
                            color = GlassEmeraldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        GlassActionButton(
                            onClick = onBackClick,
                            tintColor = GlassCyanFill,
                            borderColor = GlassCyanBorder,
                            cornerRadius = 14.dp,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                val currentWord = words[currentIndex]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassLinearProgress(
                        progress = (currentIndex + 1).toFloat() / words.size,
                        modifier = Modifier.fillMaxWidth(),
                        height = 8.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Word ${currentIndex + 1} of ${words.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMetaTranslucent
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 22.dp,
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
                                "Type the Uzbek translation:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextBodyTranslucent
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
                                    onSpeakUs = { viewModel.speakWordWithAccent(currentWord.word, false) },
                                    onSpeakUk = { viewModel.speakWordWithAccent(currentWord.word, true) }
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
                                        tagSuffix = "practice_${currentWord.id}"
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

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        label = { Text("Uzbek translation") },
                        placeholder = { Text("e.g. ${currentWord.uzbekTranslation.take(2)}...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("typing_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = glassTextFieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitAnswer() })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassActionButton(
                        onClick = { submitAnswer() },
                        enabled = !isSubmitting && userInput.isNotBlank(),
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

                    feedback?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCorrect) GlassEmeraldFill else GlassRoseFill,
                            border = BorderStroke(1.dp, if (isCorrect) GlassEmeraldBorder else GlassRoseBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                color = if (isCorrect) GlassEmeraldAccent else GlassRoseAccent,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
