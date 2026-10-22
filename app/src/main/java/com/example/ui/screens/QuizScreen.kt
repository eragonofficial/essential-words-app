package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VocabularyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, quizFinished) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                if (!quizFinished) {
                    isDisqualified = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
        topBar = {
            TopAppBar(
                title = { Text("Quiz: Book $bookNumber • Unit $unitNumber") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                CircularProgressIndicator()
            } else if (isDisqualified) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Disqualified",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Assessment Attempt Invalidated",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Anti-Cheat Policy: You left or exited the app during an active examination. Your current attempt has been auto-failed to ensure assessment integrity.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                currentIndex = 0
                                score = 0
                                isDisqualified = false
                                quizFinished = false
                                selectedOption = null
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Restart Assessment from Beginning")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Exit Quiz")
                        }
                    }
                }
            } else if (quizFinished) {
                // QUIZ RESULTS & IMMEDIATE FEEDBACK SUMMARY
                val totalQ = words.size.coerceAtLeast(1)
                val percent = (score * 100) / totalQ
                val isPassed = score >= totalQ / 2

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isPassed) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        modifier = Modifier.size(90.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPassed) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                                contentDescription = null,
                                tint = if (isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (isPassed) "Unit Quiz Passed! 🎉" else "Keep Practicing!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Score: $score of $totalQ questions ($percent%)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("finish_quiz_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (isPassed) "Continue & Unlock Next Unit" else "Review Unit & Try Again",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (currentIndex < words.size) {
                val currentWord = words[currentIndex]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Progress bar across quiz
                        LinearProgressIndicator(
                            progress = { (currentIndex + 1).toFloat() / words.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary
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
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Score: $score",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Target Word Prompt
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "What is the correct definition of:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentWord.word.uppercase(),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (currentWord.phonetic.isNotBlank()) {
                                    Text(
                                        text = currentWord.phonetic,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Options with Immediate Interactive Feedback
                        options.forEachIndexed { index, optionText ->
                            val isSelected = selectedOption == index
                            val isCorrect = index == correctAnswerIndex

                            val backgroundColor = when {
                                selectedOption == null -> MaterialTheme.colorScheme.surface
                                isCorrect -> Color(0xFFDCFCE7) // Light Green
                                isSelected -> Color(0xFFFEE2E2) // Light Red
                                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            }

                            val borderColor = when {
                                selectedOption == null -> MaterialTheme.colorScheme.outlineVariant
                                isCorrect -> Color(0xFF10B981)
                                isSelected -> Color(0xFFEF4444)
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = selectedOption == null) {
                                        selectedOption = index
                                        if (index == correctAnswerIndex) {
                                            score++
                                        }
                                    }
                                    .testTag("quiz_option_$index"),
                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
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
                                            selectedOption != null && isCorrect -> Color(0xFF10B981)
                                            selectedOption != null && isSelected -> Color(0xFFEF4444)
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${('A' + index)}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (selectedOption != null && (isCorrect || isSelected)) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (selectedOption != null && isCorrect) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                                    } else if (selectedOption != null && isSelected) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }

                        // Immediate Feedback Card upon Selection
                        AnimatedVisibility(visible = selectedOption != null) {
                            val isAnswerCorrect = selectedOption == correctAnswerIndex
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAnswerCorrect) Color(0xFFECFDF5) else Color(0xFFFFF1F2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isAnswerCorrect) "✓ Correct!" else "✗ Not quite!",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isAnswerCorrect) Color(0xFF047857) else Color(0xFFBE123C)
                                    )
                                    if (currentWord.uzbekTranslation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "🇺🇿 ${currentWord.word}: ${currentWord.uzbekTranslation}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF0D9488)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedOption != null) {
                        Button(
                            onClick = {
                                if (currentIndex + 1 < words.size) {
                                    currentIndex++
                                } else {
                                    quizFinished = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("next_question_button"),
                            shape = RoundedCornerShape(14.dp)
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
