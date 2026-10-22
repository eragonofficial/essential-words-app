package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.viewmodel.VocabularyViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val progressList by viewModel.allProgress.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    val bookmarkedCount by viewModel.bookmarkedCount.collectAsState()
    val leaderboard by viewModel.top5Leaderboard.collectAsState()

    val totalWords = 2400
    val totalUnits = 120
    val passedUnits = progressList.count { it.isPassed }
    val progressPercent = if (totalWords > 0) (learnedCount.toFloat() / totalWords * 100).toInt() else 0
    val progressFraction = if (totalWords > 0) learnedCount.toFloat() / totalWords else 0f

    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormatSelected by remember { mutableStateOf("JSON") }
    var exportedPreviewContent by remember { mutableStateOf<String?>(null) }

    fun shareExportedFile(fileName: String, content: String, mimeType: String) {
        try {
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { it.write(content.toByteArray(Charsets.UTF_8)) }

            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                )
            } catch (e: Exception) {
                Uri.fromFile(cacheFile)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "4000 Essential English Words - Progress Backup")
                putExtra(Intent.EXTRA_TEXT, "Here is my vocabulary learning progress backup file.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Share Backup File"))
            Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Dashboard & Stats") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("export_backup_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Daily Streak Counter Card with Motivation Badge
            item {
                val streak by viewModel.dailyStreak.collectAsState()
                val (badgeTitle, badgeMsg) = com.example.util.StreakManager.getMotivationBadge(streak)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🔥", fontSize = 28.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$streak Day Streak!",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFF92400E)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFD97706)
                                ) {
                                    Text(
                                        text = badgeTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = badgeMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }
            }

            // Mastery Progress Dashboard
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Mastery Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$progressPercent% of 4000 words mastered", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Summary Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Words Mastered", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$learnedCount", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text("of $totalWords total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Units Completed", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$passedUnits", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                            Text("of $totalUnits units", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Pie / Circular Chart Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Overall Vocabulary Mastery",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                            val animatedAngle by animateFloatAsState(
                                targetValue = (learnedCount.toFloat() / totalWords) * 360f,
                                label = "pieProgress"
                            )

                            Canvas(modifier = Modifier.size(160.dp)) {
                                // Background circle (unlearned)
                                drawArc(
                                    color = Color(0xFFE2E8F0),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 32f, cap = StrokeCap.Round)
                                )
                                // Foreground arc (learned)
                                if (animatedAngle > 0f) {
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            listOf(Color(0xFF2563EB), Color(0xFF10B981), Color(0xFF2563EB))
                                        ),
                                        startAngle = -90f,
                                        sweepAngle = animatedAngle,
                                        useCenter = false,
                                        style = Stroke(width = 32f, cap = StrokeCap.Round)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$progressPercent%",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Learned",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFF10B981), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Learned ($learnedCount)", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFFE2E8F0), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remaining (${totalWords - learnedCount})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Book Completion Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Completion by Book (Units Passed)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        for (bookNum in 1..4) {
                            val unitsInBook = progressList.filter { it.bookNumber == bookNum }
                            val passedInBook = unitsInBook.count { it.isPassed }
                            val frac = if (unitsInBook.isNotEmpty()) passedInBook.toFloat() / 30f else 0f

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Essential English Words $bookNum", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text("$passedInBook / 30 units", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { frac },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = when (bookNum) {
                                        1 -> Color(0xFF2563EB)
                                        2 -> Color(0xFF7C3AED)
                                        3 -> Color(0xFF0D9488)
                                        else -> Color(0xFFEA580C)
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Local Leaderboard Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("top_5_leaderboard_card"),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFEAB308))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Top 5 Quiz Champions",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "Top 5 Users",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (leaderboard.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No quizzes completed yet.\nFinish any Unit Quiz to record your score here!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            leaderboard.forEachIndexed { index, entry ->
                                val medal = when (index) {
                                    0 -> "🥇"
                                    1 -> "🥈"
                                    2 -> "🥉"
                                    else -> "#${index + 1}"
                                }
                                val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (index < 3) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color.Transparent)
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(medal, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                "${entry.userName} • Book ${entry.bookNumber} Unit ${entry.unitNumber}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (entry.score >= 8) Color(0xFF10B981) else Color(0xFF3B82F6)
                                    ) {
                                        Text(
                                            "${entry.score}/${entry.totalQuestions} pts",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Progress Backup")
                }
            },
            text = {
                Column {
                    Text(
                        "Export your vocabulary mastery, passed units, and learned words to your device storage as a JSON or CSV file.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select format:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = exportFormatSelected == "JSON",
                            onClick = { exportFormatSelected = "JSON" },
                            label = { Text("JSON (.json)") }
                        )
                        FilterChip(
                            selected = exportFormatSelected == "CSV",
                            onClick = { exportFormatSelected = "CSV" },
                            label = { Text("CSV (.csv)") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isJson = exportFormatSelected == "JSON"
                        viewModel.exportBackup(isJson) { content ->
                            val ext = if (isJson) "json" else "csv"
                            val mime = if (isJson) "application/json" else "text/csv"
                            val fileName = "4000_essential_words_backup_${System.currentTimeMillis()}.$ext"
                            shareExportedFile(fileName, content, mime)
                            showExportDialog = false
                        }
                    }
                ) {
                    Text("Export & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
