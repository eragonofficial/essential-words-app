package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UnitProgressEntity
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLinearProgress
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.theme.GlassAmberAccent
import com.example.ui.theme.GlassAmberBorder
import com.example.ui.theme.GlassAmberFill
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassEmeraldAccent
import com.example.ui.theme.GlassEmeraldBorder
import com.example.ui.theme.GlassEmeraldFill
import com.example.ui.theme.GlassIndigoBorder
import com.example.ui.theme.GlassIndigoFill
import com.example.ui.theme.GlassPinkAccent
import com.example.ui.theme.GlassPinkBorder
import com.example.ui.theme.GlassPinkFill
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier1Fill
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

    val displayUnits = if (bookProgress.isNotEmpty()) {
        bookProgress
    } else {
        (1..30).map { uNum ->
            UnitProgressEntity(
                unitId = "${bookNumber}_$uNum",
                bookNumber = bookNumber,
                unitNumber = uNum,
                unitTitle = UnitTitlesHelper.getTitle(bookNumber, uNum),
                isUnlocked = true,
                isPassed = false,
                score = 0
            )
        }
    }

    val glassPalettes = listOf(
        Pair(GlassRoyalBlueFill, GlassRoyalBlueBorder),
        Pair(GlassVioletFill, GlassVioletBorder),
        Pair(GlassCyanFill, GlassCyanBorder),
        Pair(GlassAmberFill, GlassAmberBorder),
        Pair(GlassPinkFill, GlassPinkBorder),
        Pair(GlassIndigoFill, GlassIndigoBorder)
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    Column {
                        Text(
                            "Essential English Words $bookNumber",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite
                        )
                        Text(
                            "Select a Unit to Study (30 Units)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMetaTranslucent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryWhite)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (progressList.isEmpty()) {
                GlassLinearProgress(
                    progress = 0.6f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    height = 4.dp
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

                    var isPressed by remember { mutableStateOf(false) }
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.97f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "unitCardBounce"
                    )

                    val (tintFill, tintBorder) = when {
                        isPassed -> GlassEmeraldFill to GlassEmeraldBorder
                        isUnlocked -> glassPalettes[(unitNum - 1) % glassPalettes.size]
                        else -> GlassTier1Fill to GlassTier1Border
                    }

                    GlassCard(
                        tier = if (isUnlocked) GlassTier.TIER_2 else GlassTier.TIER_1,
                        cornerRadius = 20.dp,
                        tintColor = tintFill,
                        borderColor = tintBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }
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
                                GlassPill(
                                    tintColor = GlassTier3Fill,
                                    borderColor = GlassTier3Border,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "UNIT $unitNum",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = if (isUnlocked) TextPrimaryWhite else TextMetaTranslucent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    when {
                                        isPassed -> {
                                            GlassPill(
                                                shape = CircleShape,
                                                tintColor = GlassAmberFill,
                                                borderColor = GlassAmberBorder
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = "Passed",
                                                        tint = GlassAmberAccent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        "Passed (${progress.score}/10)",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = GlassAmberAccent
                                                    )
                                                }
                                            }
                                        }
                                        isUnlocked -> {
                                            GlassPill(
                                                shape = CircleShape,
                                                tintColor = GlassEmeraldFill,
                                                borderColor = GlassEmeraldBorder
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.LockOpen,
                                                        contentDescription = "Unlocked",
                                                        tint = GlassEmeraldAccent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        "Unlocked",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = GlassEmeraldAccent
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
                                            GlassPill(
                                                shape = CircleShape,
                                                tintColor = GlassTier3Fill,
                                                borderColor = GlassTier3Border
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = "Locked",
                                                        tint = TextMetaTranslucent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        "Locked",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = TextMetaTranslucent
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isUnlocked) TextPrimaryWhite else TextMetaTranslucent,
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
                                    color = if (isUnlocked) TextBodyTranslucent else TextMetaTranslucent
                                )

                                if (isUnlocked) {
                                    GlassActionButton(
                                        onClick = { onQuizClick(bookNumber, unitNum) },
                                        tintColor = GlassTier3Fill,
                                        borderColor = GlassCyanBorder,
                                        cornerRadius = 12.dp,
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .height(38.dp)
                                            .testTag("quiz_btn_${bookNumber}_$unitNum")
                                    ) {
                                        Icon(
                                            Icons.Default.Quiz,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = GlassCyanAccent
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Quiz",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimaryWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Final Assessment / Comprehensive Exam (Rose/Amber Warning Glass Card)
                item {
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 20.dp,
                        tintColor = GlassRoseFill,
                        borderColor = GlassRoseBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("final_exam_card")
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
                                GlassPill(
                                    tintColor = GlassRoseFill,
                                    borderColor = GlassRoseBorder,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "FINAL ASSESSMENT",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = GlassRoseAccent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = GlassRoseAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Book $bookNumber Comprehensive Final Exam",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Once you enter the Final Exam, all units of Book $bookNumber automatically lock to prevent cheating. Exiting the app invalidates the attempt.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextBodyTranslucent
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            GlassActionButton(
                                onClick = {
                                    viewModel.enterFinalExam(bookNumber)
                                    onQuizClick(bookNumber, 1)
                                },
                                tintColor = GlassRoseFill,
                                borderColor = GlassRoseBorder,
                                cornerRadius = 14.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_final_exam_btn")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = GlassRoseAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enter Final Exam (Auto-lock units)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
