package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UnitProgressEntity
import com.example.util.UnitTitlesHelper
import com.example.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitListScreen(
    bookNumber: Int,
    viewModel: VocabularyViewModel,
    onUnitClick: (Int, Int) -> Unit,
    onQuizClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(bookNumber) {
        viewModel.ensureUnitsLoaded(bookNumber)
    }

    val progressList by viewModel.allProgress.collectAsState()
    val bookProgress = progressList
        .filter { it.bookNumber == bookNumber }
        .sortedBy { it.unitNumber }

    // Fallback UI state: automatically pre-populates all 30 units so screen never turns blank
    val displayUnits = if (bookProgress.isNotEmpty()) {
        bookProgress
    } else {
        (1..30).map { uNum ->
            UnitProgressEntity(
                unitId = "${bookNumber}_$uNum",
                bookNumber = bookNumber,
                unitNumber = uNum,
                unitTitle = UnitTitlesHelper.getTitle(bookNumber, uNum),
                isUnlocked = true, // All units unlocked by default
                isPassed = false,
                score = 0
            )
        }
    }

    // Preset energetic, vibrant gradient palettes for unlocked units
    val gradientPalettes = listOf(
        listOf(Color(0xFF2563EB), Color(0xFF06B6D4)), // Cobalt to Cyan
        listOf(Color(0xFF7C3AED), Color(0xFFC084FC)), // Violet to Light Purple
        listOf(Color(0xFF059669), Color(0xFF10B981)), // Emerald to Mint
        listOf(Color(0xFFEA580C), Color(0xFFFBBF24)), // Orange to Amber
        listOf(Color(0xFFDB2777), Color(0xFFF472B6)), // Pink to Rose
        listOf(Color(0xFF4F46E5), Color(0xFF818CF8))  // Indigo to Lavender
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Essential English Words $bookNumber",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Select a Unit to Study (30 Units)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (progressList.isEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayUnits, key = { "${it.bookNumber}_${it.unitNumber}" }) { progress ->
                    val unitNum = progress.unitNumber
                    val isUnlocked = progress.isUnlocked
                val isPassed = progress.isPassed
                val title = if (progress.unitTitle.isNotBlank()) {
                    progress.unitTitle
                } else {
                    UnitTitlesHelper.getTitle(bookNumber, unitNum)
                }

                // Interactive bounce animation state
                var isPressed by remember { mutableStateOf(false) }
                val animatedScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.96f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "unitCardBounce"
                )

                val gradient = if (isPassed) {
                    listOf(Color(0xFF0D9488), Color(0xFF10B981)) // Emerald Passed Gradient
                } else if (isUnlocked) {
                    gradientPalettes[(unitNum - 1) % gradientPalettes.size]
                } else {
                    listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)) // Slate Locked Gradient
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                        .shadow(
                            elevation = if (isUnlocked) 6.dp else 1.dp,
                            shape = RoundedCornerShape(20.dp),
                            clip = false
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(gradient))
                        .pointerInput(isUnlocked) {
                            if (isUnlocked) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed = true
                                        tryAwaitRelease()
                                        isPressed = false
                                    },
                                    onTap = {
                                        onUnitClick(bookNumber, unitNum)
                                    }
                                )
                            }
                        }
                        .testTag("unit_card_${bookNumber}_$unitNum")
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
                            // Badge with Unit Number
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUnlocked) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "UNIT $unitNum",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = if (isUnlocked) Color.White else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Status Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                when {
                                    isPassed -> {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFFBBF24)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = "Passed",
                                                    tint = Color(0xFF78350F),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "Passed (${progress.score}/10)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF78350F)
                                                )
                                            }
                                        }
                                    }
                                    isUnlocked -> {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF10B981)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.LockOpen,
                                                    contentDescription = "Unlocked",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "Unlocked",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF94A3B8)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "Locked",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Topic Title prominently displayed
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isUnlocked) Color.White else Color(0xFF334155),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isUnlocked) "20 Essential Words" else "Complete previous unit to unlock",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isUnlocked) Color.White.copy(alpha = 0.85f) else Color(0xFF64748B)
                            )

                            if (isUnlocked) {
                                Button(
                                    onClick = { onQuizClick(bookNumber, unitNum) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF1E293B)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("quiz_btn_${bookNumber}_$unitNum")
                                ) {
                                    Icon(
                                        Icons.Default.Quiz,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF2563EB)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Quiz",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Final Assessment / Comprehensive Exam
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("final_exam_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "FINAL ASSESSMENT",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Book $bookNumber Comprehensive Final Exam",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Once you enter the Final Exam, all units of Book $bookNumber automatically lock to prevent cheating. Exiting the app invalidates the attempt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.enterFinalExam(bookNumber)
                                onQuizClick(bookNumber, 1)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_final_exam_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enter Final Exam (Auto-lock units)")
                        }
                    }
                }
            }
        }
    }
}
}
