package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedWordsScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    val bookmarkedWords by viewModel.bookmarkedWords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Words") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(bookmarkedWords) { wordEntity ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = wordEntity.word, style = MaterialTheme.typography.titleMedium)
                            Text(text = wordEntity.definition, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.toggleBookmark(wordEntity.id, false) }) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }
}
