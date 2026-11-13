package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassAmberAccent
import com.example.ui.theme.GlassAmberBorder
import com.example.ui.theme.GlassAmberFill
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassEmeraldAccent
import com.example.ui.theme.GlassEmeraldBorder
import com.example.ui.theme.GlassEmeraldFill
import com.example.ui.theme.GlassRoyalBlueAccent
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassVioletAccent
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill

@Composable
fun PartOfSpeechBadge(
    partOfSpeech: String,
    modifier: Modifier = Modifier,
    tagSuffix: String = ""
) {
    val cleanPos = partOfSpeech.trim()
    if (cleanPos.isBlank()) return

    val (containerColor, borderColor, contentColor) = when (cleanPos.lowercase()) {
        "n." -> Triple(GlassCyanFill, GlassCyanBorder, GlassCyanAccent)
        "v." -> Triple(GlassVioletFill, GlassVioletBorder, GlassVioletAccent)
        "adj." -> Triple(GlassEmeraldFill, GlassEmeraldBorder, GlassEmeraldAccent)
        "adv." -> Triple(GlassAmberFill, GlassAmberBorder, GlassAmberAccent)
        else -> Triple(GlassRoyalBlueFill, GlassRoyalBlueBorder, GlassRoyalBlueAccent)
    }

    val fullLabel = when (cleanPos.lowercase()) {
        "n." -> "noun"
        "v." -> "verb"
        "adj." -> "adjective"
        "adv." -> "adverb"
        "prep." -> "preposition"
        "conj." -> "conjunction"
        "pron." -> "pronoun"
        "interj." -> "interjection"
        else -> cleanPos
    }

    val badgeTag = if (tagSuffix.isNotBlank()) "pos_badge_$tagSuffix" else "pos_badge"

    Surface(
        shape = RoundedCornerShape(9.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .semantics { contentDescription = "Part of speech: $fullLabel" }
            .testTag(badgeTag)
    ) {
        Text(
            text = cleanPos,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            ),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
