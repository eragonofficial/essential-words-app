package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLinearProgress
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
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.GlassTier1Fill
import com.example.ui.theme.GlassTier2Border
import com.example.ui.theme.GlassTier2Fill
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    bookNumber: Int,
    unitNumber: Int,
    viewModel: VocabularyViewModel,
    onQuizPassed: () -> Unit,
    onBackClick: () -> Unit
) {
    val words by viewModel.currentUnitWords.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }
    var isDisqualified by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var options by remember { mutableStateOf<List<String>>(emptyList()) }
    var correctAnswerIndex by remember { mutableStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, quizFinished) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && !quizFinished) {
                isDisqualified = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(bookNumber, unitNumber) {
        viewModel.loadWordsForUnit(bookNumber, unitNumber)
    }

    LaunchedEffect(currentIndex, words) {
        if (words.isNotEmpty() && currentIndex < words.size) {
            val currentWord = words[currentIndex]
            val otherDefs = words.filter { it.id != currentWord.id }.shuffled().take(3).map {
                if (it.definition.isNotBlank()) it.definition else it.englishDefinition
            }
            val correctDef = if (currentWord.definition.isNotBlank()) currentWord.definition else currentWord.englishDefinition
            val allOpts = (otherDefs + correctDef).shuffled()
            options = allOpts
            correctAnswerIndex = allOpts.indexOf(correctDef)
            selectedOption = null
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    Text(
                        "Quiz: Book $bookNumber • Unit $unitNumber",
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
            } else if (isDisqualified) {
                GlassCard(
                    tier = GlassTier.TIER_2,
                    cornerRadius = 24.dp,
                    tintColor = GlassRoseFill,
                    borderColor = GlassRoseBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Disqualified",
                            tint = GlassRoseAccent,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Assessment Attempt Invalidated",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Anti-Cheat Policy: You left or exited the app during an active examination. Your current attempt has been auto-failed to ensure assessment integrity.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextBodyTranslucent,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        GlassActionButton(
                            onClick = {
                                currentIndex = 0
                                score = 0
                                isDisqualified = false
                                quizFinished = false
                                selectedOption = null
                            },
                            tintColor = GlassRoseFill,
                            borderColor = GlassRoseBorder,
                            cornerRadius = 14.dp,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Restart Assessment from Beginning", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        GlassActionButton(
                            onClick = onBackClick,
                            tintColor = GlassTier3Fill,
                            borderColor = GlassTier3Border,
                            cornerRadius = 14.dp,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Exit Quiz")
                        }
                    }
                }
            } else if (quizFinished) {
                val totalQ = words.size.coerceAtLeast(1)
                val percent = (score * 100) / totalQ
                val isPassed = score >= totalQ / 2

                GlassCard(
                    tier = GlassTier.TIER_2,
                    cornerRadius = 26.dp,
                    tintColor = if (isPassed) GlassEmeraldFill else GlassRoseFill,
                    borderColor = if (isPassed) GlassEmeraldBorder else GlassRoseBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        GlassSurfaceBox(
                            tier = GlassTier.TIER_3,
                            cornerRadius = 45.dp,
                            tintColor = if (isPassed) GlassEmeraldFill else GlassRoseFill,
                            borderColor = if (isPassed) GlassEmeraldBorder else GlassRoseBorder,
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(90.dp).clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPassed) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                                contentDescription = null,
                                tint = if (isPassed) GlassEmeraldAccent else GlassRoseAccent,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = if (isPassed) "Unit Quiz Passed! 🎉" else "Keep Practicing!",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimaryWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Score: $score of $totalQ questions ($percent%)",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isPassed) GlassEmeraldAccent else GlassRoseAccent,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isPassed) {
                                "Awesome job! Your score is saved to the Leaderboard and the next unit is now unlocked!"
                            } else {
                                "Score at least 50% to pass and unlock the next unit. Try again!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextBodyTranslucent,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        GlassActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.recordQuizScore("Study Master", bookNumber, unitNumber, score, totalQ)
                                    if (isPassed) {
                                        viewModel.passUnit(bookNumber, unitNumber, percent)
                                        onQuizPassed()
                                    } else {
                                        onBackClick()
                                    }
                                }
                            },
                            tintColor = if (isPassed) GlassEmeraldFill else GlassCyanFill,
                            borderColor = if (isPassed) GlassEmeraldBorder else GlassCyanBorder,
                            cornerRadius = 14.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("finish_quiz_button")
                        ) {
                            Text(
                                if (isPassed) "Continue & Unlock Next Unit" else "Review Unit & Try Again",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (currentIndex < words.size) {
                val currentWord = words[currentIndex]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        GlassLinearProgress(
                            progress = (currentIndex + 1).toFloat() / words.size,
                            modifier = Modifier.fillMaxWidth(),
                            height = 8.dp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${currentIndex + 1} of ${words.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMetaTranslucent
                            )
                            Text(
                                text = "Score: $score",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = GlassCyanAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Target Word Glass Prompt Card
                        GlassCard(
                            tier = GlassTier.TIER_2,
                            cornerRadius = 20.dp,
                            tintColor = GlassCyanFill,
                            borderColor = GlassCyanBorder,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "What is the correct definition of:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextBodyTranslucent
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentWord.word.uppercase(),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = TextPrimaryWhite
                                )
                                if (currentWord.partOfSpeech.isNotBlank() || currentWord.phonetic.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PartOfSpeechBadge(
                                            partOfSpeech = currentWord.partOfSpeech,
                                            tagSuffix = "quiz_${currentWord.id}"
                                        )
                                        if (currentWord.phonetic.isNotBlank()) {
                                            Text(
                                                text = currentWord.phonetic,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMetaTranslucent
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                DualAudioButtons(
                                    word = currentWord.word,
                                    onSpeakUs = { viewModel.speakWordWithAccent(currentWord.word, false) },
                                    onSpeakUk = { viewModel.speakWordWithAccent(currentWord.word, true) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Glass Option Buttons with Tinted Emerald/Rose Interactive Feedback
                        options.forEachIndexed { index, optionText ->
                            val isSelected = selectedOption == index
                            val isCorrect = index == correctAnswerIndex

                            val fillColor = when {
                                selectedOption == null -> GlassTier2Fill
                                isCorrect -> GlassEmeraldFill
                                isSelected -> GlassRoseFill
                                else -> GlassTier1Fill
                            }

                            val borderColor = when {
                                selectedOption == null -> GlassTier2Border
                                isCorrect -> GlassEmeraldBorder
                                isSelected -> GlassRoseBorder
                                else -> GlassTier2Border.copy(alpha = 0.4f)
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = fillColor,
                                border = BorderStroke(1.25.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(enabled = selectedOption == null) {
                                        selectedOption = index
                                        if (index == correctAnswerIndex) {
                                            score++
                                        }
                                    }
                                    .testTag("quiz_option_$index")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            selectedOption != null && isCorrect -> GlassEmeraldFill
                                            selectedOption != null && isSelected -> GlassRoseFill
                                            else -> GlassTier3Fill
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            when {
                                                selectedOption != null && isCorrect -> GlassEmeraldBorder
                                                selectedOption != null && isSelected -> GlassRoseBorder
                                                else -> GlassTier3Border
                                            }
                                        ),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${('A' + index)}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimaryWhite
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = TextPrimaryWhite,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (selectedOption != null && isCorrect) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GlassEmeraldAccent)
                                    } else if (selectedOption != null && isSelected) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = GlassRoseAccent)
                                    }
                                }
                            }
                        }

                        // Tinted Emerald / Rose Glass Feedback Banner
                        AnimatedVisibility(visible = selectedOption != null) {
                            val isAnswerCorrect = selectedOption == correctAnswerIndex
                            GlassSurfaceBox(
                                tier = GlassTier.TIER_3,
                                cornerRadius = 14.dp,
                                tintColor = if (isAnswerCorrect) GlassEmeraldFill else GlassRoseFill,
                                borderColor = if (isAnswerCorrect) GlassEmeraldBorder else GlassRoseBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isAnswerCorrect) "✓ Correct!" else "✗ Not quite!",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isAnswerCorrect) GlassEmeraldAccent else GlassRoseAccent
                                    )
                                    if (currentWord.uzbekTranslation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "🇺🇿 ${currentWord.word}: ${currentWord.uzbekTranslation}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimaryWhite
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedOption != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassActionButton(
                            onClick = {
                                if (currentIndex + 1 < words.size) {
                                    currentIndex++
                                } else {
                                    quizFinished = true
                                }
                            },
                            tintColor = GlassCyanFill,
                            borderColor = GlassCyanBorder,
                            cornerRadius = 14.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("next_question_button")
                        ) {
                            Text(
                                if (currentIndex + 1 < words.size) "Next Question →" else "View Results & Save Score",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
