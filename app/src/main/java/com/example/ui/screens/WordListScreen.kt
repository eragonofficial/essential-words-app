package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WordEntity
import com.example.util.UnitTitlesHelper
import com.example.viewmodel.VocabularyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class WordListTab(val label: String, val icon: ImageVector) {
    LIST("List View", Icons.Default.ViewList),
    FLASHCARDS("Flashcard Mode", Icons.Default.Style),
    TYPING_QUIZ("Typing Quiz", Icons.Default.Edit)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    bookNumber: Int,
    unitNumber: Int,
    viewModel: VocabularyViewModel,
    onQuizClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit,
    onPracticeClick: (Int, Int) -> Unit = { _, _ -> },
    onWordClick: ((Long) -> Unit)? = null
) {
    val words by viewModel.currentUnitWords.collectAsState()
    val isUkAccent by viewModel.isUkAccent.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiExplanation by viewModel.aiExplanation.collectAsState()
    var selectedWordForAi by remember { mutableStateOf<WordEntity?>(null) }

    // Primary horizontal mode tab selector
    var currentTab by remember { mutableStateOf(WordListTab.LIST) }
    var flashcardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    // Typing Quiz State
    var typingIndex by remember { mutableStateOf(0) }
    var typingInput by remember { mutableStateOf("") }
    var typingFeedback by remember { mutableStateOf<String?>(null) }
    var isTypingCorrect by remember { mutableStateOf(false) }
    var typingScore by remember { mutableStateOf(0) }
    var typingFinished by remember { mutableStateOf(false) }
    var isCheckingTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 90)
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

    // Speech-to-Text Pronunciation Voice Analysis State
    var activePronunciationWord by remember { mutableStateOf<WordEntity?>(null) }
    var pronunciationResultText by remember { mutableStateOf<String?>(null) }
    var isPronunciationCorrect by remember { mutableStateOf(false) }
    var isListeningPronunciation by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningPronunciation = false
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()?.trim()?.lowercase() ?: ""
            val target = activePronunciationWord?.word?.trim()?.lowercase() ?: ""

            if (spoken.isNotBlank() && target.isNotBlank()) {
                val isMatch = spoken == target || spoken.contains(target) || target.contains(spoken)
                isPronunciationCorrect = isMatch
                pronunciationResultText = if (isMatch) {
                    "Perfect! You pronounced \"$spoken\" correctly! (100% Match) 🎉"
                } else {
                    "Keep trying! You said \"$spoken\". Target word: \"$target\""
                }
                if (isMatch) {
                    try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200) } catch (e: Exception) {}
                } else {
                    try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 200) } catch (e: Exception) {}
                }
            }
        }
    }

    // System Back button handling: smoothly returns from Flashcard/Typing mode to List View first
    BackHandler {
        if (currentTab != WordListTab.LIST) {
            currentTab = WordListTab.LIST
        } else {
            onBackClick()
        }
    }

    LaunchedEffect(bookNumber, unitNumber) {
        viewModel.loadWordsForUnit(bookNumber, unitNumber)
    }

    val unitTitle = UnitTitlesHelper.getTitle(bookNumber, unitNumber)
    val learnedCountInUnit = words.count { it.isLearned }
    val progressFraction = if (words.isNotEmpty()) learnedCountInUnit.toFloat() / words.size else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "unitWordsProgress"
    )

    fun submitTypingAnswer() {
        if (isCheckingTyping || words.isEmpty() || typingIndex !in words.indices) return
        val currentWord = words[typingIndex]
        val cleanInput = typingInput.trim().lowercase()
        val cleanTarget = currentWord.uzbekTranslation.trim().lowercase()

        isCheckingTyping = true
        val matched = cleanInput.isNotBlank() && (
                cleanInput == cleanTarget ||
                        cleanTarget.split(",").map { it.trim() }.contains(cleanInput) ||
                        (cleanTarget.contains(cleanInput) && cleanInput.length >= 3)
                )

        if (matched) {
            isTypingCorrect = true
            typingFeedback = "To'g'ri! Correct! 🎉"
            typingScore++
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 180)
            } catch (e: Exception) {}
        } else {
            isTypingCorrect = false
            typingFeedback = "Noto'g'ri. To'g'risi: ${currentWord.uzbekTranslation}"
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 220)
            } catch (e: Exception) {}
        }

        coroutineScope.launch {
            delay(1400)
            if (typingIndex < words.size - 1) {
                typingIndex++
                typingInput = ""
                typingFeedback = null
                isCheckingTyping = false
            } else {
                typingFinished = true
                isCheckingTyping = false
                viewModel.recordQuizScore("Typing Quiz", bookNumber, unitNumber, typingScore, words.size)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Book $bookNumber • Unit $unitNumber",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            unitTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentTab != WordListTab.LIST) {
                            currentTab = WordListTab.LIST
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Accent Switcher Button (UK 🇬🇧 vs US 🇺🇸 toggle)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isUkAccent) Color(0xFF1E3A8A) else Color(0xFF991B1B),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.toggleAccent() }
                            .testTag("accent_toggle_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isUkAccent) "🇬🇧 UK" else "🇺🇸 US",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch accent",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // Unit Quiz Button
                    FilledTonalButton(
                        onClick = { onQuizClick(bookNumber, unitNumber) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("start_quiz_button"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quiz", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Clean Single Horizontal Top Tab Selector (List View | Flashcard Mode | Typing Quiz)
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mode_tab_selector")
            ) {
                WordListTab.values().forEach { tab ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = {
                            currentTab = tab
                            if (tab == WordListTab.FLASHCARDS) isFlipped = false
                        },
                        text = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        icon = {
                            Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            when (currentTab) {
                WordListTab.FLASHCARDS -> {
                    // FLASHCARD ACTIVE RECALL MODE
                    if (words.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val currentWord = words[flashcardIndex.coerceIn(0, words.lastIndex)]

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Flashcard Progress Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Card ${flashcardIndex + 1} of ${words.size}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FilterChip(
                                    selected = currentWord.isLearned,
                                    onClick = { viewModel.toggleLearned(currentWord.id, !currentWord.isLearned) },
                                    label = { Text(if (currentWord.isLearned) "Learned ✓" else "Mark Learned") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF10B981),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }

                            LinearProgressIndicator(
                                progress = { (flashcardIndex + 1).toFloat() / words.size },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Flip Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .shadow(8.dp, shape = RoundedCornerShape(24.dp))
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { isFlipped = !isFlipped }
                                    .padding(24.dp)
                                    .testTag("active_recall_flashcard"),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(
                                    targetState = isFlipped,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "flashcardFlipAnimation"
                                ) { flipped ->
                                    if (!flipped) {
                                        // FRONT: English Word & Phonetics only
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(60.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.speakWord(currentWord.word) },
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Icon(
                                                        Icons.Default.VolumeUp,
                                                        contentDescription = "Pronounce",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(24.dp))

                                            Text(
                                                text = currentWord.word,
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 1.sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )

                                            if (currentWord.phonetic.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = currentWord.phonetic,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(36.dp))

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.TouchApp,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        "Tap card to flip definition & translation",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // BACK: Full Definition, Uzbek Translation, Example Sentences, Synonyms
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
                                                Text(
                                                    text = currentWord.word,
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                IconButton(
                                                    onClick = { viewModel.speakWord(currentWord.word) }
                                                ) {
                                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Uzbek translation
                                            if (currentWord.uzbekTranslation.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFF0D9488).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "🇺🇿 ${currentWord.uzbekTranslation}",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = Color(0xFF0F766E),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                            }

                                            // English Definition with Dynamic Accent Flag
                                            val def = if (currentWord.definition.isNotBlank()) currentWord.definition else currentWord.englishDefinition
                                            if (def.isNotBlank()) {
                                                val activeFlag = if (isUkAccent) "🇬🇧 UK" else "🇺🇸 US"
                                                Text(
                                                    "$activeFlag Definition:",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(def, style = MaterialTheme.typography.bodyMedium)
                                                Spacer(modifier = Modifier.height(10.dp))
                                            }

                                            // Uzbek Definition
                                            if (currentWord.uzbekDefinition.isNotBlank()) {
                                                Text("🇺🇿 O'zbekcha izohi:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF0F766E))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(currentWord.uzbekDefinition, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.height(10.dp))
                                            }

                                            // Example Sentence
                                            val exEn = if (currentWord.exampleSentenceEn.isNotBlank()) currentWord.exampleSentenceEn else currentWord.exampleSentence
                                            if (exEn.isNotBlank()) {
                                                Text("Example:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("“$exEn”", style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic))
                                                if (currentWord.exampleSentenceUz.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("“${currentWord.exampleSentenceUz}”", style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.outline)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                "Tap anywhere to flip back 🔄",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Navigation Buttons (Prev, Flip, Next)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (flashcardIndex > 0) {
                                            flashcardIndex--
                                            isFlipped = false
                                        }
                                    },
                                    enabled = flashcardIndex > 0,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Previous")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = { isFlipped = !isFlipped },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isFlipped) "Show Word" else "Flip Card")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = {
                                        if (flashcardIndex < words.size - 1) {
                                            flashcardIndex++
                                            isFlipped = false
                                        }
                                    },
                                    enabled = flashcardIndex < words.size - 1,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Next")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                WordListTab.TYPING_QUIZ -> {
                    // INLINE TYPING QUIZ MODE
                    if (words.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (typingFinished) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
                                        text = "Typing Quiz Finished! 🏆",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Score: $typingScore of ${words.size} correct",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            typingIndex = 0
                                            typingScore = 0
                                            typingInput = ""
                                            typingFeedback = null
                                            typingFinished = false
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Practice Again")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { currentTab = WordListTab.LIST }) {
                                        Text("Return to Word List")
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
                            LinearProgressIndicator(
                                progress = { (typingIndex + 1).toFloat() / words.size },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Word ${typingIndex + 1} of ${words.size}",
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
                                        "Translate this word to Uzbek:",
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
                                value = typingInput,
                                onValueChange = { typingInput = it },
                                label = { Text("Enter Uzbek translation") },
                                placeholder = { Text("e.g. ${currentWord.uzbekTranslation.take(2)}...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("typing_input"),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { submitTypingAnswer() })
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { submitTypingAnswer() },
                                enabled = !isCheckingTyping && typingInput.isNotBlank(),
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

                            typingFeedback?.let {
                                Text(
                                    text = it,
                                    color = if (isTypingCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                WordListTab.LIST -> {
                    // NEAT VERTICAL WORD LIST VIEW
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header card with unit stats and progress bar
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Unit Mastery",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "$learnedCountInUnit of ${words.size} Words Learned",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }

                        // Word Detail Cards in stacked, neat vertical layout
                        items(words, key = { it.id }) { wordEntity ->
                            var isCardPressed by remember { mutableStateOf(false) }
                            val cardScale by animateFloatAsState(
                                targetValue = if (isCardPressed) 0.98f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "wordCardBounce"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = cardScale
                                        scaleY = cardScale
                                    }
                                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isCardPressed = true
                                                tryAwaitRelease()
                                                isCardPressed = false
                                            },
                                            onTap = {
                                                onWordClick?.invoke(wordEntity.id)
                                            }
                                        )
                                    }
                                    .testTag("word_card_${wordEntity.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)
                                ) {
                                    // LINE 1: English word with phonetic and Uzbek translation
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = wordEntity.word,
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                if (wordEntity.phonetic.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                    ) {
                                                        Text(
                                                            text = wordEntity.phonetic,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Uzbek translation
                                            if (wordEntity.uzbekTranslation.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = "🇺🇿 ${wordEntity.uzbekTranslation}",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF0D9488)
                                                )
                                            }
                                        }

                                        // Audio Playback (TTS with active UK/US accent) and Bookmark
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isUkAccent) Color(0xFF1E3A8A).copy(alpha = 0.12f) else Color(0xFF991B1B).copy(alpha = 0.12f),
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (isUkAccent) "🇬🇧 UK" else "🇺🇸 US",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isUkAccent) Color(0xFF1E3A8A) else Color(0xFF991B1B),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }

                                            FilledIconButton(
                                                onClick = { viewModel.speakWord(wordEntity.word) },
                                                shape = CircleShape,
                                                colors = IconButtonDefaults.filledIconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .testTag("tts_btn_${wordEntity.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.VolumeUp,
                                                    contentDescription = "Pronounce with ${if (isUkAccent) "UK (🇬🇧)" else "US (🇺🇸)"} accent",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            IconButton(
                                                onClick = { viewModel.toggleBookmark(wordEntity.id, !wordEntity.isBookmarked) },
                                                modifier = Modifier.testTag("bookmark_btn_${wordEntity.id}")
                                            ) {
                                                Icon(
                                                    if (wordEntity.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = "Bookmark",
                                                    tint = if (wordEntity.isBookmarked) Color(0xFFEAB308) else MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // LINE 2: English Definition with Dynamic Accent Flag (🇬🇧 UK vs 🇺🇸 US)
                                    val engDef = if (wordEntity.definition.isNotBlank()) wordEntity.definition else wordEntity.englishDefinition
                                    if (engDef.isNotBlank()) {
                                        val activeFlag = if (isUkAccent) "🇬🇧 UK" else "🇺🇸 US"
                                        Text(
                                            text = "$activeFlag Definition:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = engDef,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // LINE 3: Uzbek Definition
                                    if (wordEntity.uzbekDefinition.isNotBlank()) {
                                        Text(
                                            text = "🇺🇿 O'zbekcha izohi:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF0F766E)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = wordEntity.uzbekDefinition,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // LINE 4: English Example Sentence
                                    val exEn = if (wordEntity.exampleSentenceEn.isNotBlank()) wordEntity.exampleSentenceEn else wordEntity.exampleSentence
                                    if (exEn.isNotBlank()) {
                                        Text(
                                            text = "Example Sentence:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "“$exEn”",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // LINE 5: Uzbek Translation of Example Sentence
                                    if (wordEntity.exampleSentenceUz.isNotBlank()) {
                                        Text(
                                            text = "Misol tarjimasi:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF0F766E)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "“${wordEntity.exampleSentenceUz}”",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // LINE 6 & 7: Synonyms and Antonyms lists
                                    if (wordEntity.synonyms.isNotBlank() || wordEntity.antonyms.isNotBlank()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (wordEntity.synonyms.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFF0284C7).copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "SYNONYMS",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = Color(0xFF0284C7),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = wordEntity.synonyms,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                        color = Color(0xFF0369A1)
                                                    )
                                                }
                                            }

                                            if (wordEntity.antonyms.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFFE11D48).copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "ANTONYMS",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = Color(0xFFE11D48),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = wordEntity.antonyms,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                        color = Color(0xFFBE123C)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // Action footer: Mark as Learned chip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilterChip(
                                            selected = wordEntity.isLearned,
                                            onClick = { viewModel.toggleLearned(wordEntity.id, !wordEntity.isLearned) },
                                            label = {
                                                Text(
                                                    if (wordEntity.isLearned) "Learned ✓" else "Mark Learned",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            leadingIcon = if (wordEntity.isLearned) {
                                                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF10B981),
                                                selectedLabelColor = Color.White,
                                                selectedLeadingIconColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("learned_chip_${wordEntity.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AI Insight Dialog
            if (selectedWordForAi != null) {
                AlertDialog(
                    onDismissRequest = { selectedWordForAi = null },
                    title = { Text("AI Insight: ${selectedWordForAi?.word}") },
                    text = {
                        if (isAiLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Text(text = aiExplanation, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedWordForAi = null }) {
                            Text("Close")
                        }
                    }
                )
            }

            // Pronunciation Result Dialog
            if (pronunciationResultText != null) {
                AlertDialog(
                    onDismissRequest = { pronunciationResultText = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isPronunciationCorrect) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPronunciationCorrect) Color(0xFF10B981) else Color(0xFFE11D48)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPronunciationCorrect) "Pronunciation Check: Passed! 🎉" else "Pronunciation Check: Practice Needed")
                        }
                    },
                    text = {
                        Column {
                            Text(
                                text = "Target Word: ${activePronunciationWord?.word}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pronunciationResultText ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { pronunciationResultText = null }) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}
