package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dictionary.GeographicEntityInfo
import com.example.data.dictionary.GlobalDictionaryEntry
import com.example.data.dictionary.SearchDirection
import com.example.data.dictionary.SearchResultTier
import com.example.ui.theme.GlassAmberAccent
import com.example.ui.theme.GlassAmberBorder
import com.example.ui.theme.GlassAmberFill
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
import com.example.ui.theme.GlassVioletAccent
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalDictionaryEntryCard(
    entry: GlobalDictionaryEntry,
    onPronounceClick: () -> Unit = {},
    onPronounceUs: () -> Unit = onPronounceClick,
    onPronounceUk: () -> Unit = onPronounceClick,
    onRelatedWordClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUzbToEng = entry.direction == SearchDirection.UZB_TO_ENG
    val geoInfo = entry.geoInfo

    GlassCard(
        tier = GlassTier.TIER_2,
        cornerRadius = 22.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("global_entry_card_${entry.headword}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (entry.tier == SearchResultTier.TIER_1_GEOGRAPHIC && geoInfo != null) {
                GeographicEntityCardContent(
                    entry = entry,
                    geo = geoInfo,
                    isUzbToEng = isUzbToEng,
                    onPronounceUs = onPronounceUs,
                    onPronounceUk = onPronounceUk
                )
            } else {
                LexicalOrFallbackCardContent(
                    entry = entry,
                    isUzbToEng = isUzbToEng,
                    onPronounceUs = onPronounceUs,
                    onPronounceUk = onPronounceUk,
                    onRelatedWordClick = onRelatedWordClick
                )
            }
        }
    }
}

@Composable
private fun GeographicEntityCardContent(
    entry: GlobalDictionaryEntry,
    geo: GeographicEntityInfo,
    isUzbToEng: Boolean,
    onPronounceUs: () -> Unit,
    onPronounceUk: () -> Unit
) {
    val primaryHeading = if (isUzbToEng) geo.nameUz else geo.nameEn
    val secondaryTitle = if (isUzbToEng) "🇬🇧 Official English Name" else "🇺🇿 Rasmiy o'zbekcha nomi"
    val secondaryValue = if (isUzbToEng) geo.nameEn else geo.nameUz
    val secondaryFill = if (isUzbToEng) GlassCyanFill else GlassEmeraldFill
    val secondaryBorder = if (isUzbToEng) GlassCyanBorder else GlassEmeraldBorder
    val secondaryAccent = if (isUzbToEng) GlassCyanAccent else GlassEmeraldAccent

    // Primary Heading with Flag Emoji + Dual Audio Pronunciation Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (geo.flagEmoji.isNotBlank()) {
                GlassSurfaceBox(
                    tier = GlassTier.TIER_3,
                    cornerRadius = 16.dp,
                    tintColor = GlassRoyalBlueFill,
                    borderColor = GlassRoyalBlueBorder,
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = geo.flagEmoji,
                        fontSize = 30.sp
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = primaryHeading,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryWhite,
                    modifier = Modifier.testTag("global_entry_heading_${entry.headword}")
                )
                GlassPill(
                    tintColor = GlassTier3Fill,
                    borderColor = GlassTier3Border,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${geo.regionEn} • ${geo.regionUz}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMetaTranslucent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        DualAudioButtons(
            word = entry.headword,
            onSpeakUs = onPronounceUs,
            onSpeakUk = onPronounceUk
        )
    }

    // Inverted Official Equivalent Pane
    GlassSurfaceBox(
        tier = GlassTier.TIER_3,
        cornerRadius = 14.dp,
        tintColor = secondaryFill,
        borderColor = secondaryBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = secondaryTitle,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = secondaryAccent
            )
            Text(
                text = secondaryValue,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimaryWhite
            )
        }
    }

    // Geographic Metadata Details Grid
    GlassSurfaceBox(
        tier = GlassTier.TIER_3,
        cornerRadius = 14.dp,
        tintColor = GlassTier3Fill,
        borderColor = GlassTier3Border,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("geo_details_panel_${entry.headword}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = GlassCyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Geographic Profile",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = GlassCyanAccent
                )
            }

            if (!geo.countryEn.equals(geo.nameEn, ignoreCase = true)) {
                GeoMetaRow(
                    label = if (isUzbToEng) "Davlat (Country)" else "Country (Davlat)",
                    value = if (isUzbToEng) "${geo.countryUz} • ${geo.countryEn}" else "${geo.countryEn} • ${geo.countryUz}"
                )
            }
            if (geo.capitalEn.isNotBlank()) {
                GeoMetaRow(
                    label = if (isUzbToEng) "Poytaxt (Capital)" else "Capital (Poytaxt)",
                    value = if (isUzbToEng) "${geo.capitalUz} • ${geo.capitalEn}" else "${geo.capitalEn} • ${geo.capitalUz}"
                )
            }
            if (geo.regionEn.isNotBlank()) {
                GeoMetaRow(
                    label = if (isUzbToEng) "Hudud / Qit'a (Region)" else "Region / Continent",
                    value = if (isUzbToEng) "${geo.regionUz} • ${geo.regionEn}" else "${geo.regionEn} • ${geo.regionUz}"
                )
            }
        }
    }
}

@Composable
private fun GeoMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = GlassEmeraldAccent,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(15.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMetaTranslucent
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextPrimaryWhite
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LexicalOrFallbackCardContent(
    entry: GlobalDictionaryEntry,
    isUzbToEng: Boolean,
    onPronounceUs: () -> Unit,
    onPronounceUk: () -> Unit,
    onRelatedWordClick: (String) -> Unit
) {
    val primaryHeading = if (isUzbToEng) {
        entry.matchedFromUzbek?.takeIf { it.isNotBlank() }
            ?: entry.primaryUzbekTranslation.ifBlank { entry.headword }
    } else {
        entry.headword
    }

    // Inverted Header + Phonetic + Dual Audio Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primaryHeading,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryWhite,
                modifier = Modifier.testTag("global_entry_heading_${entry.headword}")
            )
            if (!isUzbToEng && entry.phonetic.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                GlassPill(
                    tintColor = GlassTier3Fill,
                    borderColor = GlassTier3Border,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = entry.phonetic,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMetaTranslucent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
        DualAudioButtons(
            word = entry.headword,
            onSpeakUs = onPronounceUs,
            onSpeakUk = onPronounceUk
        )
    }

    if (isUzbToEng) {
        // Luminous English Equivalent Glass Pane (strict heading inversion)
        GlassSurfaceBox(
            tier = GlassTier.TIER_3,
            cornerRadius = 14.dp,
            tintColor = GlassCyanFill,
            borderColor = GlassCyanBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = GlassCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "🇬🇧 English Equivalent",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassCyanAccent
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.headword,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimaryWhite
                    )
                    if (entry.phonetic.isNotBlank()) {
                        GlassPill(
                            tintColor = GlassTier3Fill,
                            borderColor = GlassTier3Border,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = entry.phonetic,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMetaTranslucent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (entry.primaryUzbekTranslation.isNotBlank() &&
                    !entry.primaryUzbekTranslation.equals(primaryHeading, ignoreCase = true)
                ) {
                    Text(
                        text = "🇺🇿 ${entry.primaryUzbekTranslation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBodyTranslucent
                    )
                }
            }
        }
    } else if (entry.primaryUzbekTranslation.isNotBlank() || entry.uzbekMeanings.isNotEmpty()) {
        // ENG -> UZB: Emerald Uzbek Translation Glass Pane
        GlassSurfaceBox(
            tier = GlassTier.TIER_3,
            cornerRadius = 14.dp,
            tintColor = GlassEmeraldFill,
            borderColor = GlassEmeraldBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🇺🇿 O'zbekcha ma'nosi",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = GlassEmeraldAccent
                )
                if (entry.primaryUzbekTranslation.isNotBlank()) {
                    Text(
                        text = entry.primaryUzbekTranslation,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimaryWhite
                    )
                }
                entry.uzbekMeanings.forEach { group ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (group.partOfSpeech.isNotBlank()) {
                            Text(
                                text = "${group.partOfSpeech}:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = GlassCyanAccent
                            )
                        }
                        Text(
                            text = group.terms.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextBodyTranslucent
                        )
                    }
                }
            }
        }
    }


    // English Definitions grouped by Part of Speech
    if (entry.meanings.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Definitions & Usage",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassCyanAccent
            )
            entry.meanings.forEach { meaning ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GlassPill(
                        tintColor = GlassVioletFill,
                        borderColor = GlassVioletBorder,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = meaning.partOfSpeech,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = GlassVioletAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    meaning.definitions.forEachIndexed { idx, defItem ->
                        Column(
                            modifier = Modifier.padding(start = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${idx + 1}. ${defItem.definition}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextBodyTranslucent
                            )
                            if (defItem.example.isNotBlank()) {
                                Text(
                                    text = "\"${defItem.example}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                    color = TextMetaTranslucent,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Synonyms & Antonyms (Always shown in Tier 2; shown in Tier 3 only when populated)
    val showThesaurusSections = entry.tier == SearchResultTier.TIER_2_DICTIONARY_THESAURUS ||
        entry.synonyms.isNotEmpty() ||
        entry.antonyms.isNotEmpty()

    if (showThesaurusSections) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Synonyms (Sinonimlar)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassEmeraldAccent
            )
            if (entry.synonyms.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.synonyms.forEach { syn ->
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No direct synonyms listed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMetaTranslucent
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Antonyms (Antonimlar)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = GlassRoseAccent
            )
            if (entry.antonyms.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.antonyms.forEach { ant ->
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No direct antonyms listed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMetaTranslucent
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
