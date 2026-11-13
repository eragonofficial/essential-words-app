package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WordEntity
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassHairlineDivider
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassModalDialog
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.PartOfSpeechBadge
import com.example.ui.components.glassFilterChipBorder
import com.example.ui.components.glassFilterChipColors
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
import com.example.ui.theme.GlassRoyalBlueAccent
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier1Fill
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassVioletAccent
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.util.UnitTitlesHelper
import com.example.viewmodel.VocabularyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class WordListTab(val label: String, val icon: ImageVector) {
    LIST("List View", Icons.Default.ViewList),
    FLASHCARDS("Flashcard Mode", Icons.Default.Style),
    TYPING_QUIZ("Typing Quiz", Icons.Default.Edit)
}

@OptIn(ExperimentalLayoutApi::class)
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

    var currentTab by remember { mutableStateOf(WordListTab.LIST) }
    var flashcardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

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
            } catch (_: Exception) {}
        }
    }

    var activePronunciationWord by remember { mutableStateOf<WordEntity?>(null) }
    var pronunciationResultText by remember { mutableStateOf<String?>(null) }
    var isPronunciationCorrect by remember { mutableStateOf(false) }

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
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 180) } catch (_: Exception) {}
        } else {
            isTypingCorrect = false
            typingFeedback = "Noto'g'ri. To'g'risi: ${currentWord.uzbekTranslation}"
            try { toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 220) } catch (_: Exception) {}
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
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    Column {
                        Text(
                            "Book $bookNumber • Unit $unitNumber",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite
                        )
                        Text(
                            unitTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMetaTranslucent,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryWhite)
                    }
                },
                actions = {
                    GlassActionButton(
                        onClick = { onQuizClick(bookNumber, unitNumber) },
                        tintColor = GlassCyanFill,
                        borderColor = GlassCyanBorder,
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(38.dp)
                            .testTag("start_quiz_button")
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp), tint = GlassCyanAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quiz", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tier 1 Frosted Glass TabRow
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = GlassTier1Fill,
                contentColor = GlassCyanAccent,
                indicator = { tabPositions ->
                    if (currentTab.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                            color = GlassCyanAccent
                        )
                    }
                },
                divider = { GlassHairlineDivider(color = GlassTier1Border) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mode_tab_selector")
            ) {
                WordListTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    Tab(
                        selected = selected,
                        onClick = {
                            currentTab = tab
                            if (tab == WordListTab.FLASHCARDS) isFlipped = false
                        },
                        text = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) GlassCyanAccent else TextBodyTranslucent
                            )
                        },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (selected) GlassCyanAccent else TextMetaTranslucent
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            when (currentTab) {
                WordListTab.FLASHCARDS -> {
                    if (words.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GlassCyanAccent)
                        }
                    } else {
                        WordListFlashcardMode(
                            words = words,
                            flashcardIndex = flashcardIndex,
                            isFlipped = isFlipped,
                            isUkAccent = isUkAccent,
                            onFlipToggle = { isFlipped = !isFlipped },
                            onPrev = {
                                if (flashcardIndex > 0) {
                                    flashcardIndex--
                                    isFlipped = false
                                }
                            },
                            onNext = {
                                if (flashcardIndex < words.size - 1) {
                                    flashcardIndex++
                                    isFlipped = false
                                }
                            },
                            onSpeakWordWithAccent = { word, isUk -> viewModel.speakWordWithAccent(word, isUk) },
                            onToggleLearned = { id, learned -> viewModel.toggleLearned(id, learned) }
                        )
                    }
                }

                WordListTab.TYPING_QUIZ -> {
                    if (words.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GlassCyanAccent)
                        }
                    } else {
                        WordListTypingQuizMode(
                            words = words,
                            typingIndex = typingIndex,
                            typingInput = typingInput,
                            typingFeedback = typingFeedback,
                            isTypingCorrect = isTypingCorrect,
                            typingScore = typingScore,
                            typingFinished = typingFinished,
                            isCheckingTyping = isCheckingTyping,
                            onInputChange = { typingInput = it },
                            onSubmitAnswer = { submitTypingAnswer() },
                            onSpeakWordWithAccent = { word, isUk -> viewModel.speakWordWithAccent(word, isUk) },
                            onPracticeAgain = {
                                typingIndex = 0
                                typingScore = 0
                                typingInput = ""
                                typingFeedback = null
                                typingFinished = false
                            },
                            onReturnToList = { currentTab = WordListTab.LIST }
                        )
                    }
                }

                WordListTab.LIST -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GlassCard(
                                tier = GlassTier.TIER_2,
                                cornerRadius = 18.dp,
                                tintColor = GlassCyanFill,
                                borderColor = GlassCyanBorder,
                                modifier = Modifier.fillMaxWidth()
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
                                            color = GlassCyanAccent
                                        )
                                        Text(
                                            "$learnedCountInUnit of ${words.size} Words Learned",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = TextBodyTranslucent
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    GlassLinearProgress(
                                        progress = animatedProgress,
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 10.dp
                                    )
                                }
                            }
                        }

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

                            GlassCard(
                                tier = GlassTier.TIER_2,
                                cornerRadius = 22.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = cardScale
                                        scaleY = cardScale
                                    }
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            FlowRow(
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = wordEntity.word,
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    color = TextPrimaryWhite,
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                                PartOfSpeechBadge(
                                                    partOfSpeech = wordEntity.partOfSpeech,
                                                    modifier = Modifier.align(Alignment.CenterVertically),
                                                    tagSuffix = "${wordEntity.id}"
                                                )
                                                if (wordEntity.phonetic.isNotBlank()) {
                                                    GlassPill(
                                                        tintColor = GlassTier3Fill,
                                                        borderColor = GlassTier3Border,
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    ) {
                                                        Text(
                                                            text = wordEntity.phonetic,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                            color = TextMetaTranslucent,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            if (wordEntity.uzbekTranslation.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "🇺🇿 ${wordEntity.uzbekTranslation}",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = GlassEmeraldAccent
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            DualAudioButtons(
                                                word = wordEntity.word,
                                                onSpeakUs = { viewModel.speakWordWithAccent(wordEntity.word, false) },
                                                onSpeakUk = { viewModel.speakWordWithAccent(wordEntity.word, true) },
                                                usTestTag = "tts_us_btn_${wordEntity.id}",
                                                ukTestTag = "tts_uk_btn_${wordEntity.id}",
                                                modifier = Modifier.testTag("tts_btn_${wordEntity.id}")
                                            )

                                            IconButton(
                                                onClick = { viewModel.toggleBookmark(wordEntity.id, !wordEntity.isBookmarked) },
                                                modifier = Modifier.testTag("bookmark_btn_${wordEntity.id}")
                                            ) {
                                                Icon(
                                                    if (wordEntity.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = "Bookmark",
                                                    tint = if (wordEntity.isBookmarked) GlassAmberAccent else TextMetaTranslucent
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    GlassHairlineDivider(color = GlassTier1Border)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val engDef = if (wordEntity.definition.isNotBlank()) wordEntity.definition else wordEntity.englishDefinition
                                    if (engDef.isNotBlank()) {
                                        Text(
                                            text = "Definition:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GlassCyanAccent
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = engDef,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextBodyTranslucent
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (wordEntity.uzbekDefinition.isNotBlank()) {
                                        Text(
                                            text = "🇺🇿 O'zbekcha izohi:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GlassEmeraldAccent
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = wordEntity.uzbekDefinition,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextBodyTranslucent
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    val exEn = if (wordEntity.exampleSentenceEn.isNotBlank()) wordEntity.exampleSentenceEn else wordEntity.exampleSentence
                                    if (exEn.isNotBlank()) {
                                        Text(
                                            text = "Example Sentence:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GlassVioletAccent
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        GlassSurfaceBox(
                                            tier = GlassTier.TIER_3,
                                            cornerRadius = 12.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "“$exEn”",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                                color = TextPrimaryWhite,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (wordEntity.exampleSentenceUz.isNotBlank()) {
                                        Text(
                                            text = "Misol tarjimasi:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GlassEmeraldAccent
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "“${wordEntity.exampleSentenceUz}”",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                            color = TextBodyTranslucent
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    if (wordEntity.synonyms.isNotBlank() || wordEntity.antonyms.isNotBlank()) {
                                        GlassSurfaceBox(
                                            tier = GlassTier.TIER_3,
                                            cornerRadius = 12.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (wordEntity.synonyms.isNotBlank()) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        GlassPill(
                                                            tintColor = GlassRoyalBlueFill,
                                                            borderColor = GlassRoyalBlueBorder,
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "SYNONYMS",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = GlassRoyalBlueAccent,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = wordEntity.synonyms,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                            color = TextBodyTranslucent
                                                        )
                                                    }
                                                }

                                                if (wordEntity.antonyms.isNotBlank()) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        GlassPill(
                                                            tintColor = GlassRoseFill,
                                                            borderColor = GlassRoseBorder,
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "ANTONYMS",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = GlassRoseAccent,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = wordEntity.antonyms,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                            color = TextBodyTranslucent
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

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
                                            colors = glassFilterChipColors(
                                                selectedFill = GlassEmeraldFill,
                                                selectedBorder = GlassEmeraldBorder
                                            ),
                                            border = glassFilterChipBorder(
                                                selected = wordEntity.isLearned,
                                                selectedBorder = GlassEmeraldBorder
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

            if (selectedWordForAi != null) {
                GlassModalDialog(
                    onDismissRequest = { selectedWordForAi = null },
                    title = {
                        Text(
                            "AI Insight: ${selectedWordForAi?.word}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        if (isAiLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GlassCyanAccent)
                            }
                        } else {
                            Text(text = aiExplanation, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedWordForAi = null }) {
                            Text("Close", color = GlassCyanAccent)
                        }
                    }
                )
            }

            if (pronunciationResultText != null) {
                GlassModalDialog(
                    onDismissRequest = { pronunciationResultText = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isPronunciationCorrect) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPronunciationCorrect) GlassEmeraldAccent else GlassRoseAccent
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
                                color = GlassCyanAccent
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
                            Text("OK", fontWeight = FontWeight.Bold, color = GlassCyanAccent)
                        }
                    }
                )
            }
        }
    }
}
