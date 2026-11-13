package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.data.dictionary.DynamicThesaurus
import com.example.data.local.WordEntity
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
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextbookSearchResultCard(
    wordEntity: WordEntity,
    searchQuery: String,
    searchLanguage: com.example.data.repository.VocabularyRepository.LocalBookSearchLanguage = com.example.data.repository.VocabularyRepository.LocalBookSearchLanguage.ALL,
    dynamicThesaurus: DynamicThesaurus?,
    onRequestThesaurus: (String) -> Unit,
    onWordClick: (Long) -> Unit,
    onSpeakUk: (String) -> Unit,
    onSpeakUs: (String) -> Unit,
    onRelatedWordClick: (String) -> Unit,
    onBookUnitClick: (Int, Int) -> Unit = { _, _ -> },
    onToggleBookmark: ((Long, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(wordEntity.word) {
        onRequestThesaurus(wordEntity.word)
    }

    val mergedSynonyms = remember(wordEntity.synonyms, dynamicThesaurus?.synonyms) {
        mergeThesaurusTerms(wordEntity.synonyms, dynamicThesaurus?.synonyms.orEmpty(), wordEntity.word)
    }
    val mergedAntonyms = remember(wordEntity.antonyms, dynamicThesaurus?.antonyms) {
        mergeThesaurusTerms(wordEntity.antonyms, dynamicThesaurus?.antonyms.orEmpty(), wordEntity.word)
    }

    val isUzbMode = searchLanguage == com.example.data.repository.VocabularyRepository.LocalBookSearchLanguage.UZBEK && wordEntity.uzbekTranslation.isNotBlank()

    GlassCard(
        tier = GlassTier.TIER_2,
        cornerRadius = 20.dp,
        onClick = { onWordClick(wordEntity.id) },
        modifier = modifier
            .fillMaxWidth()
            .testTag("book_search_result_${wordEntity.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Prominent Title (Uzbek in Uzbek mode, English otherwise) + Badge + Phonetic + Book/Unit Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = highlightTextbookQuery(if (isUzbMode) wordEntity.uzbekTranslation else wordEntity.word, searchQuery),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isUzbMode) GlassEmeraldAccent else TextPrimaryWhite,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    PartOfSpeechBadge(
                        partOfSpeech = wordEntity.partOfSpeech,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        tagSuffix = "book_search_${wordEntity.id}"
                    )
                    if (!isUzbMode && wordEntity.phonetic.isNotBlank()) {
                        GlassPill(
                            tintColor = GlassTier3Fill,
                            borderColor = GlassTier3Border,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text(
                                text = wordEntity.phonetic,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMetaTranslucent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GlassPill(
                        tintColor = GlassCyanFill,
                        borderColor = GlassCyanBorder,
                        shape = RoundedCornerShape(10.dp),
                        onClick = { onBookUnitClick(wordEntity.bookNumber, wordEntity.unitNumber) },
                        modifier = Modifier.testTag("book_unit_badge_${wordEntity.id}")
                    ) {
                        Text(
                            text = "Book ${wordEntity.bookNumber} • Unit ${wordEntity.unitNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GlassCyanAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    if (onToggleBookmark != null) {
                        IconButton(
                            onClick = { onToggleBookmark(wordEntity.id, !wordEntity.isBookmarked) },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("book_search_bookmark_${wordEntity.id}")
                        ) {
                            Icon(
                                imageVector = if (wordEntity.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (wordEntity.isBookmarked) "Remove bookmark" else "Save word",
                                tint = if (wordEntity.isBookmarked) GlassAmberAccent else TextMetaTranslucent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (isUzbMode) {
                // In Uzbek mode, show English equivalent prominently underneath
                GlassSurfaceBox(
                    tier = GlassTier.TIER_3,
                    cornerRadius = 12.dp,
                    tintColor = GlassCyanFill,
                    borderColor = GlassCyanBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "🇬🇧 English Equivalent",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GlassCyanAccent
                            )
                            Text(
                                text = wordEntity.word,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimaryWhite
                            )
                        }
                        if (wordEntity.phonetic.isNotBlank()) {
                            Text(
                                text = wordEntity.phonetic,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMetaTranslucent
                            )
                        }
                    }
                }
            }

            // US & UK Dual Audio Pills with active glow/wave feedback
            DualAudioButtons(
                word = wordEntity.word,
                onSpeakUs = { onSpeakUs(wordEntity.word) },
                onSpeakUk = { onSpeakUk(wordEntity.word) },
                usTestTag = "book_word_speak_us_${wordEntity.id}",
                ukTestTag = "book_word_speak_uk_${wordEntity.id}",
                modifier = Modifier.fillMaxWidth()
            )

            // Uzbek Translation & Definition
            if (wordEntity.uzbekTranslation.isNotBlank() || wordEntity.uzbekDefinition.isNotBlank()) {
                GlassSurfaceBox(
                    tier = GlassTier.TIER_3,
                    cornerRadius = 12.dp,
                    tintColor = GlassEmeraldFill,
                    borderColor = GlassEmeraldBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (wordEntity.uzbekTranslation.isNotBlank()) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("🇺🇿 ")
                                    }
                                    append(highlightTextbookQuery(wordEntity.uzbekTranslation, searchQuery))
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GlassEmeraldAccent
                            )
                        }
                        if (wordEntity.uzbekDefinition.isNotBlank()) {
                            Text(
                                text = highlightTextbookQuery(wordEntity.uzbekDefinition, searchQuery),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextBodyTranslucent
                            )
                        }
                    }
                }
            }

            // English Definition
            val def = wordEntity.englishDefinition.ifBlank { wordEntity.definition }
            if (def.isNotBlank()) {
                Text(
                    text = highlightTextbookQuery(def, searchQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextBodyTranslucent
                )
            }

            // Example Sentences (EN + UZ)
            val exEn = wordEntity.exampleSentenceEn.ifBlank { wordEntity.exampleSentence }
            val exUz = wordEntity.exampleSentenceUz
            if (exEn.isNotBlank() || exUz.isNotBlank()) {
                GlassSurfaceBox(
                    tier = GlassTier.TIER_3,
                    cornerRadius = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Example Sentences",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GlassCyanAccent
                        )
                        if (exEn.isNotBlank()) {
                            Text(
                                text = "“$exEn”",
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                color = TextPrimaryWhite
                            )
                        }
                        if (exUz.isNotBlank()) {
                            Text(
                                text = "🇺🇿 “$exUz”",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextBodyTranslucent
                            )
                        }
                    }
                }
            }

            GlassHairlineDivider(color = GlassTier1Border)

            // Dynamic Synonyms & Antonyms beneath card details
            if (dynamicThesaurus?.isLoading == true && mergedSynonyms.isEmpty() && mergedAntonyms.isEmpty()) {
                GlassLinearProgress(
                    progress = 0.65f,
                    modifier = Modifier.fillMaxWidth(),
                    height = 4.dp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Synonyms (Sinonimlar)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = GlassEmeraldAccent
                )
                if (mergedSynonyms.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        mergedSynonyms.forEach { syn ->
                            Surface(
                                onClick = { onRelatedWordClick(syn) },
                                shape = RoundedCornerShape(10.dp),
                                color = GlassEmeraldFill,
                                border = BorderStroke(1.dp, GlassEmeraldBorder)
                            ) {
                                Text(
                                    text = syn,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimaryWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (dynamicThesaurus?.isLoading == true) "Loading synonyms..." else "No synonyms found",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMetaTranslucent
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Antonyms (Antonimlar)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = GlassRoseAccent
                )
                if (mergedAntonyms.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        mergedAntonyms.forEach { ant ->
                            Surface(
                                onClick = { onRelatedWordClick(ant) },
                                shape = RoundedCornerShape(10.dp),
                                color = GlassRoseFill,
                                border = BorderStroke(1.dp, GlassRoseBorder)
                            ) {
                                Text(
                                    text = ant,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimaryWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (dynamicThesaurus?.isLoading == true) "Loading antonyms..." else "No antonyms found",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMetaTranslucent
                    )
                }
            }
        }
    }
}

fun mergeThesaurusTerms(localCsv: String, dynamicList: List<String>, headword: String): List<String> {
    val result = linkedMapOf<String, String>()
    val cleanHeadword = headword.trim().lowercase(Locale.ROOT)
    localCsv.split(",", ";")
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.lowercase(Locale.ROOT).equals(cleanHeadword) }
        .forEach { term ->
            result.putIfAbsent(term.lowercase(Locale.ROOT), term)
        }
    dynamicList
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.lowercase(Locale.ROOT).equals(cleanHeadword) }
        .forEach { term ->
            result.putIfAbsent(term.lowercase(Locale.ROOT), term)
        }
    return result.values.take(12)
}

fun highlightTextbookQuery(text: String, query: String): AnnotatedString {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) return AnnotatedString(text)

    val normQuery = normalizeForHighlight(cleanQuery)
    val normText = normalizeForHighlight(text)

    // Fast path when apostrophe normalization preserves 1:1 character positions
    if (normText.length == text.length && normQuery.isNotEmpty()) {
        val ranges = findHighlightRanges(normText, normQuery)
        if (ranges.isNotEmpty()) {
            return buildAnnotatedStringWithRanges(text, ranges)
        }
    }

    // Fallback: apostrophe-stripped matching (e.g., "qorqqan" matches "qo'rqqan" or "qoʻrqqan")
    val strippedQuery = normQuery.replace("'", "")
    if (strippedQuery.isNotEmpty()) {
        val strippedMap = ArrayList<Int>(text.length)
        val strippedChars = StringBuilder(text.length)
        for (i in text.indices) {
            val ch = normalizeCharForHighlight(text[i])
            if (ch != '\'') {
                strippedChars.append(ch)
                strippedMap.add(i)
            }
        }
        val strippedText = strippedChars.toString()
        val ranges = mutableListOf<IntRange>()
        var searchFrom = 0
        while (searchFrom < strippedText.length) {
            val idx = strippedText.indexOf(strippedQuery, searchFrom)
            if (idx == -1) break
            val origStart = strippedMap[idx]
            val origEnd = strippedMap[idx + strippedQuery.length - 1]
            ranges.add(origStart..origEnd)
            searchFrom = idx + strippedQuery.length
        }
        if (ranges.isNotEmpty()) {
            return buildAnnotatedStringWithRanges(text, ranges)
        }
    }

    return AnnotatedString(text)
}

private fun normalizeCharForHighlight(ch: Char): Char = when (ch) {
    'ʻ', 'ʼ', '’', '‘', '`', '′', 'ʹ' -> '\''
    else -> ch.lowercaseChar()
}

private fun normalizeForHighlight(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
        sb.append(normalizeCharForHighlight(ch))
    }
    return sb.toString()
}

private fun findHighlightRanges(haystack: String, needle: String): List<IntRange> {
    val ranges = mutableListOf<IntRange>()
    var startIndex = haystack.indexOf(needle)
    while (startIndex != -1) {
        ranges.add(startIndex until (startIndex + needle.length))
        startIndex = haystack.indexOf(needle, startIndex + needle.length)
    }
    return ranges
}

private fun buildAnnotatedStringWithRanges(text: String, ranges: List<IntRange>): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        for (range in ranges) {
            if (range.first > cursor) {
                append(text.substring(cursor, range.first))
            }
            val endExclusive = (range.last + 1).coerceAtMost(text.length)
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = GlassCyanAccent)) {
                append(text.substring(range.first, endExclusive))
            }
            cursor = endExclusive
        }
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
