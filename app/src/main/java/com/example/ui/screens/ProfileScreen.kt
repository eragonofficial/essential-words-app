package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ui.components.DualAudioButtons
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSurfaceBox
import com.example.ui.components.GlassTier
import com.example.ui.components.GlassTopAppBar
import com.example.ui.components.PartOfSpeechBadge
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassEmeraldAccent
import com.example.ui.theme.GlassRoseAccent
import com.example.ui.theme.GlassRoseBorder
import com.example.ui.theme.GlassRoseFill
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier1Fill
import com.example.ui.theme.GlassVioletAccent
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import com.example.viewmodel.VocabularyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: VocabularyViewModel,
    onSignOut: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val learnedWords by viewModel.learnedWords.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    val currentUser = Firebase.auth.currentUser

    var notificationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopAppBar(
                title = {
                    Text(
                        "Profile & Mastery Stats",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlassSurfaceBox(
                            tier = GlassTier.TIER_3,
                            cornerRadius = 32.dp,
                            tintColor = GlassCyanFill,
                            borderColor = GlassCyanBorder,
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(64.dp).clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GlassCyanAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentUser?.displayName ?: "English Learner",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = currentUser?.email ?: "cloud_sync@essentialwords.app",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMetaTranslucent
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$learnedCount",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassCyanAccent
                                )
                                Text(text = "Mastered", style = MaterialTheme.typography.bodySmall, color = TextBodyTranslucent)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "6",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassVioletAccent
                                )
                                Text(text = "Books", style = MaterialTheme.typography.bodySmall, color = TextBodyTranslucent)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "180",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GlassEmeraldAccent
                                )
                                Text(text = "Units", style = MaterialTheme.typography.bodySmall, color = TextBodyTranslucent)
                            }
                        }
                    }
                }
            }

            item {
                GlassCard(
                    tier = GlassTier.TIER_2,
                    cornerRadius = 18.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Study Reminder",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryWhite
                            )
                            Text(
                                text = "Receive a daily notification to practice your vocabulary.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextBodyTranslucent
                            )
                        }
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { enabled ->
                                notificationEnabled = enabled
                                if (enabled) {
                                    val workRequest = PeriodicWorkRequestBuilder<com.example.worker.DailyReminderWorker>(24, TimeUnit.HOURS).build()
                                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                        "daily_vocab_reminder",
                                        ExistingPeriodicWorkPolicy.KEEP,
                                        workRequest
                                    )
                                } else {
                                    WorkManager.getInstance(context).cancelUniqueWork("daily_vocab_reminder")
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimaryWhite,
                                checkedTrackColor = GlassCyanAccent.copy(alpha = 0.7f),
                                uncheckedThumbColor = TextMetaTranslucent,
                                uncheckedTrackColor = GlassTier1Fill,
                                uncheckedBorderColor = GlassTier1Border
                            ),
                            modifier = Modifier.testTag("reminder_switch")
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Mastered Words ($learnedCount)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryWhite,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (learnedWords.isEmpty()) {
                item {
                    GlassCard(
                        tier = GlassTier.TIER_1,
                        cornerRadius = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No words marked learned yet. Tap 'Mark Learned' in unit study view!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextBodyTranslucent
                            )
                        }
                    }
                }
            } else {
                items(learnedWords, key = { it.id }) { wordEntity ->
                    GlassCard(
                        tier = GlassTier.TIER_2,
                        cornerRadius = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FlowRow(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = wordEntity.word,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimaryWhite,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    PartOfSpeechBadge(
                                        partOfSpeech = wordEntity.partOfSpeech,
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        tagSuffix = "profile_${wordEntity.id}"
                                    )
                                    if (wordEntity.phonetic.isNotBlank()) {
                                        Text(
                                            text = wordEntity.phonetic,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMetaTranslucent,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = wordEntity.definition,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextBodyTranslucent
                                )
                            }
                            DualAudioButtons(
                                word = wordEntity.word,
                                onSpeakUs = { viewModel.speakWordWithAccent(wordEntity.word, false) },
                                onSpeakUk = { viewModel.speakWordWithAccent(wordEntity.word, true) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                GlassActionButton(
                    onClick = {
                        Firebase.auth.signOut()
                        viewModel.refreshAuthUser()
                        onSignOut()
                    },
                    tintColor = GlassRoseFill,
                    borderColor = GlassRoseBorder,
                    cornerRadius = 14.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = GlassRoseAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = TextPrimaryWhite)
                }
            }
        }
    }
}
