package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.viewmodel.VocabularyViewModel

@Composable
fun ReviewScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit
) {
    SpacedRepetitionReviewScreen(
        viewModel = viewModel,
        onBackClick = onBackClick
    )
}
