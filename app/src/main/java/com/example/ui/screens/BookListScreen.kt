package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    viewModel: VocabularyViewModel,
    onBookClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onSynonymsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDashboardClick: () -> Unit = {}
) {
    val progressList by viewModel.allProgress.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()

    val books = listOf(
        Triple(1, "Essential English Words 1", "Unit-based vocabulary study"),
        Triple(2, "Essential English Words 2", "Unit-based vocabulary study"),
        Triple(3, "Essential English Words 3", "Unit-based vocabulary study"),
        Triple(4, "Essential English Words 4", "Unit-based vocabulary study")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("4000 Essential Words") },
                actions = {
                    IconButton(onClick = onDashboardClick, modifier = Modifier.testTag("dashboard_action_button")) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Dashboard & Leaderboard")
                    }
                    IconButton(onClick = onSearchClick, modifier = Modifier.testTag("search_action_button")) {
                        Icon(Icons.Default.Search, contentDescription = "Global Search")
                    }
                    IconButton(onClick = onSynonymsClick, modifier = Modifier.testTag("synonyms_action_button")) {
                        Icon(Icons.Default.CompareArrows, contentDescription = "Synonyms & Antonyms")
                    }
                    IconButton(onClick = onProfileClick, modifier = Modifier.testTag("profile_action_button")) {
                        Icon(Icons.Default.Person, contentDescription = "Profile & Stats")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Hero Banner Image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "App Logo Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                val streak by viewModel.dailyStreak.collectAsState()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onDashboardClick() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Stats",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mastered",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$learnedCount",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "of 2400 words",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onDashboardClick() },
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Daily Streak",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = androidx.compose.ui.graphics.Color(0xFF92400E)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$streak Days",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = androidx.compose.ui.graphics.Color(0xFFD97706)
                            )
                            Text(
                                text = if (streak > 0) "Keep it going! 🔥" else "Start today! ⚡",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color(0xFF78350F)
                            )
                        }
                    }
                }
            }

            items(books) { (bookNum, title, desc) ->
                val bookUnits = progressList.filter { it.bookNumber == bookNum }
                val passedUnits = bookUnits.count { it.isPassed }
                val progressFraction = if (bookUnits.isNotEmpty()) passedUnits.toFloat() / bookUnits.size else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onBookClick(bookNum) }
                        .testTag("book_card_$bookNum"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Book",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Completed: $passedUnits / 30 Units",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
