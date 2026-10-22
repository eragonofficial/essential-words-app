package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VocabularyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (e: Exception) {}
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
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            } catch (e: Exception) {}
        } else {
            isCorrect = false
            feedback = "Incorrect. Correct: ${currentWord.uzbekTranslation}"
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
            } catch (e: Exception) {}
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
        topBar = {
            TopAppBar(
                title = { Text("Typing Practice: B$bookNumber • U$unitNumber") },
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
            } else if (quizFinished) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Practice Completed! 🏆",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Score: $score of ${words.size} words",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            } else {
                val currentWord = words[currentIndex]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / words.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Word ${currentIndex + 1} of ${words.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Type the Uzbek translation:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = currentWord.word,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.speakWord(currentWord.word) }) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Pronounce", tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            if (currentWord.phonetic.isNotBlank()) {
                                Text(
                                    text = currentWord.phonetic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitAnswer() })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { submitAnswer() },
                        enabled = !isSubmitting && userInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_translation_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Answer")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    feedback?.let {
                        Text(
                            text = it,
                            color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
