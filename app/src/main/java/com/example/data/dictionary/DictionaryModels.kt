package com.example.data.dictionary

enum class SearchDirection(val label: String, val hint: String) {
    ENG_TO_UZB(
        label = "ENG ➔ UZB",
        hint = "Word, country, city, slang, or phrase (e.g. Uzbekistan, serendipity)..."
    ),
    UZB_TO_ENG(
        label = "UZB ➔ ENG",
        hint = "So'z, davlat, shahar yoki ibora kiriting (masalan: O'zbekiston, Toshkent)..."
    )
}

enum class SearchResultTier(val badgeLabel: String) {
    TIER_1_GEOGRAPHIC("Tier 1 • Geographic Entity"),
    TIER_2_DICTIONARY_THESAURUS("Tier 2 • Dictionary & Thesaurus"),
    TIER_3_TRANSLATION_FALLBACK("Tier 3 • Universal Translation")
}

data class GeographicEntityInfo(
    val entityType: String,
    val flagEmoji: String,
    val nameEn: String,
    val nameUz: String,
    val countryEn: String,
    val countryUz: String,
    val capitalEn: String,
    val capitalUz: String,
    val regionEn: String,
    val regionUz: String
)

data class DictionaryDefinitionItem(
    val definition: String,
    val example: String = "",
    val uzbekDefinition: String = ""
)

data class DictionaryMeaningGroup(
    val partOfSpeech: String,
    val definitions: List<DictionaryDefinitionItem>
)

data class UzbekMeaningGroup(
    val partOfSpeech: String,
    val terms: List<String>
)

data class GlobalDictionaryEntry(
    val headword: String,
    val matchedFromUzbek: String? = null,
    val phonetic: String = "",
    val audioUrl: String? = null,
    val audioUrlUs: String? = null,
    val audioUrlUk: String? = null,
    val primaryUzbekTranslation: String = "",
    val uzbekMeanings: List<UzbekMeaningGroup> = emptyList(),
    val meanings: List<DictionaryMeaningGroup> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val sourceLabel: String = "Global Dictionary",
    val direction: SearchDirection = SearchDirection.ENG_TO_UZB,
    val tier: SearchResultTier = SearchResultTier.TIER_2_DICTIONARY_THESAURUS,
    val geoInfo: GeographicEntityInfo? = null
)

data class GlobalSearchUiState(
    val query: String = "",
    val direction: SearchDirection = SearchDirection.ENG_TO_UZB,
    val isLoading: Boolean = false,
    val entries: List<GlobalDictionaryEntry> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val autoSwitchMessage: String? = null
)

data class DynamicThesaurus(
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val isLoading: Boolean = false
)
