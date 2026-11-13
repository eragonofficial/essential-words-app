package com.example.data.dictionary

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class GlobalSearchResult(
    val entries: List<GlobalDictionaryEntry>,
    val suggestions: List<String>,
    val errorMessage: String? = null
)

private data class OfflineLexiconItem(
    val word: String,
    val translation: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String,
    val synonyms: List<String>,
    val antonyms: List<String>
)

private data class GeoDatabaseIndex(
    val allEntities: List<GeographicEntityInfo>,
    val engExactMap: Map<String, List<GeographicEntityInfo>>,
    val engLooseMap: Map<String, List<GeographicEntityInfo>>,
    val uzbExactMap: Map<String, List<GeographicEntityInfo>>,
    val uzbLooseMap: Map<String, List<GeographicEntityInfo>>
)

class GlobalDictionaryRepository(context: Context) {

    private val appContext = context.applicationContext
    private val service = GlobalDictionaryService()
    private val prefs = appContext.getSharedPreferences("global_dictionary_prefs", Context.MODE_PRIVATE)

    @Volatile
    private var cachedLexicon: List<OfflineLexiconItem>? = null

    @Volatile
    private var cachedEngMap: Map<String, OfflineLexiconItem>? = null

    @Volatile
    private var cachedGeoIndex: GeoDatabaseIndex? = null

    private suspend fun ensureGeoDatabaseLoaded(): GeoDatabaseIndex = withContext(Dispatchers.IO) {
        cachedGeoIndex?.let { return@withContext it }

        synchronized(this@GlobalDictionaryRepository) {
            cachedGeoIndex?.let { return@withContext it }

            val allEntities = mutableListOf<GeographicEntityInfo>()
            val engExact = LinkedHashMap<String, MutableList<GeographicEntityInfo>>()
            val engLoose = LinkedHashMap<String, MutableList<GeographicEntityInfo>>()
            val uzbExact = LinkedHashMap<String, MutableList<GeographicEntityInfo>>()
            val uzbLoose = LinkedHashMap<String, MutableList<GeographicEntityInfo>>()

            fun addGeoMapping(
                keyRaw: String,
                entity: GeographicEntityInfo,
                exactMap: MutableMap<String, MutableList<GeographicEntityInfo>>,
                looseMap: MutableMap<String, MutableList<GeographicEntityInfo>>
            ) {
                val norm = normalizeTextKey(keyRaw)
                if (norm.isBlank()) return
                val exactList = exactMap.getOrPut(norm) { mutableListOf() }
                if (exactList.none { it == entity }) exactList.add(entity)

                val loose = stripApostrophesAndPunct(norm)
                if (loose.isNotBlank()) {
                    val looseList = looseMap.getOrPut(loose) { mutableListOf() }
                    if (looseList.none { it == entity }) looseList.add(entity)
                }
            }

            fun indexEntity(
                entity: GeographicEntityInfo,
                extraAliasesEn: List<String>,
                extraAliasesUz: List<String>
            ) {
                allEntities.add(entity)
                addGeoMapping(entity.nameEn, entity, engExact, engLoose)
                extraAliasesEn.forEach { addGeoMapping(it, entity, engExact, engLoose) }

                addGeoMapping(entity.nameUz, entity, uzbExact, uzbLoose)
                extraAliasesUz.forEach { addGeoMapping(it, entity, uzbExact, uzbLoose) }
            }

            try {
                val jsonText = appContext.assets.open("geographic_entities.json")
                    .bufferedReader()
                    .use { it.readText() }
                val root = JSONObject(jsonText)

                // 1. Countries + their Capitals
                val countriesArr = root.optJSONArray("countries") ?: JSONArray()
                for (i in 0 until countriesArr.length()) {
                    val obj = countriesArr.optJSONObject(i) ?: continue
                    val flag = obj.optString("flag", "").trim()
                    val nameEn = obj.optString("nameEn", "").trim()
                    val nameUz = obj.optString("nameUz", "").trim()
                    val capitalEn = obj.optString("capitalEn", "").trim()
                    val capitalUz = obj.optString("capitalUz", "").trim()
                    val regionEn = obj.optString("regionEn", "").trim()
                    val regionUz = obj.optString("regionUz", "").trim()
                    if (nameEn.isBlank() || nameUz.isBlank()) continue

                    val aliasesEn = jsonStringList(obj.optJSONArray("aliasesEn"))
                    val aliasesUz = jsonStringList(obj.optJSONArray("aliasesUz"))

                    val sameAsCapital = capitalEn.equals(nameEn, ignoreCase = true)
                    val countryEntity = GeographicEntityInfo(
                        entityType = if (sameAsCapital) "Country & Capital" else "Country",
                        flagEmoji = flag,
                        nameEn = nameEn,
                        nameUz = nameUz,
                        countryEn = nameEn,
                        countryUz = nameUz,
                        capitalEn = capitalEn,
                        capitalUz = capitalUz,
                        regionEn = regionEn,
                        regionUz = regionUz
                    )
                    indexEntity(countryEntity, aliasesEn, aliasesUz)

                    if (!sameAsCapital && capitalEn.isNotBlank() && capitalUz.isNotBlank()) {
                        val capitalAliasesEn = buildList {
                            if (capitalEn.contains(",")) {
                                add(capitalEn.substringBefore(",").trim())
                            }
                            if (capitalEn.equals("Washington, D.C.", ignoreCase = true)) {
                                add("Washington DC")
                                add("Washington D.C.")
                            }
                        }
                        val capitalEntity = GeographicEntityInfo(
                            entityType = "Capital City",
                            flagEmoji = flag,
                            nameEn = capitalEn,
                            nameUz = capitalUz,
                            countryEn = nameEn,
                            countryUz = nameUz,
                            capitalEn = capitalEn,
                            capitalUz = capitalUz,
                            regionEn = regionEn,
                            regionUz = regionUz
                        )
                        indexEntity(capitalEntity, capitalAliasesEn, emptyList())
                    }
                }

                // 2. Major Global & Uzbek Cities
                val citiesArr = root.optJSONArray("cities") ?: JSONArray()
                for (i in 0 until citiesArr.length()) {
                    val obj = citiesArr.optJSONObject(i) ?: continue
                    val flag = obj.optString("flag", "").trim()
                    val nameEn = obj.optString("nameEn", "").trim()
                    val nameUz = obj.optString("nameUz", "").trim()
                    val countryEn = obj.optString("countryEn", "").trim()
                    val countryUz = obj.optString("countryUz", "").trim()
                    val capitalEn = obj.optString("capitalEn", "").trim()
                    val capitalUz = obj.optString("capitalUz", "").trim()
                    val regionEn = obj.optString("regionEn", "").trim()
                    val regionUz = obj.optString("regionUz", "").trim()
                    if (nameEn.isBlank() || nameUz.isBlank()) continue

                    val aliasesEn = jsonStringList(obj.optJSONArray("aliasesEn"))
                    val aliasesUz = jsonStringList(obj.optJSONArray("aliasesUz"))

                    val cityEntity = GeographicEntityInfo(
                        entityType = "Major City",
                        flagEmoji = flag,
                        nameEn = nameEn,
                        nameUz = nameUz,
                        countryEn = countryEn,
                        countryUz = countryUz,
                        capitalEn = capitalEn,
                        capitalUz = capitalUz,
                        regionEn = regionEn,
                        regionUz = regionUz
                    )
                    indexEntity(cityEntity, aliasesEn, aliasesUz)
                }
            } catch (e: Exception) {
                Log.w("GlobalDictRepo", "Failed to load geographic_entities.json: ${e.message}")
            }

            val index = GeoDatabaseIndex(
                allEntities = allEntities,
                engExactMap = engExact,
                engLooseMap = engLoose,
                uzbExactMap = uzbExact,
                uzbLooseMap = uzbLoose
            )
            cachedGeoIndex = index
            index
        }
    }

    private suspend fun ensureLexiconLoaded(): Pair<List<OfflineLexiconItem>, Map<String, OfflineLexiconItem>> =
        withContext(Dispatchers.IO) {
            val existingList = cachedLexicon
            val existingMap = cachedEngMap
            if (existingList != null && existingMap != null) {
                return@withContext existingList to existingMap
            }

            synchronized(this@GlobalDictionaryRepository) {
                val doubleCheckList = cachedLexicon
                val doubleCheckMap = cachedEngMap
                if (doubleCheckList != null && doubleCheckMap != null) {
                    return@withContext doubleCheckList to doubleCheckMap
                }

                val list = mutableListOf<OfflineLexiconItem>()
                val map = HashMap<String, OfflineLexiconItem>()
                try {
                    val jsonText = appContext.assets.open("global_lexicon.json")
                        .bufferedReader()
                        .use { it.readText() }
                    val arr = JSONArray(jsonText)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val word = obj.optString("word", "").trim()
                        if (word.isBlank()) continue
                        val syns = jsonStringList(obj.optJSONArray("synonyms"))
                        val ants = jsonStringList(obj.optJSONArray("antonyms"))
                        val item = OfflineLexiconItem(
                            word = word,
                            translation = obj.optString("translation", "").trim(),
                            phonetic = obj.optString("phonetic", "").trim(),
                            partOfSpeech = obj.optString("partOfSpeech", "word").trim(),
                            definition = obj.optString("definition", "").trim(),
                            example = obj.optString("example", "").trim(),
                            synonyms = syns,
                            antonyms = ants
                        )
                        list.add(item)
                        map[word.lowercase(Locale.ROOT)] = item
                    }
                } catch (e: Exception) {
                    Log.w("GlobalDictRepo", "Failed to load global_lexicon.json: ${e.message}")
                }

                // Also load the 3,600 book words from words_data.json as a fast local bonus
                try {
                    val wordsJsonText = appContext.assets.open("words_data.json")
                        .bufferedReader()
                        .use { it.readText() }
                    val wordsArr = JSONArray(wordsJsonText)
                    for (i in 0 until wordsArr.length()) {
                        val obj = wordsArr.getJSONObject(i)
                        val word = obj.optString("word", "").trim()
                        if (word.isBlank()) continue
                        val key = word.lowercase(Locale.ROOT)
                        if (map.containsKey(key)) continue
                        val syns = obj.optString("synonyms", "")
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        val ants = obj.optString("antonyms", "")
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        val item = OfflineLexiconItem(
                            word = word,
                            translation = obj.optString("uzbekTranslation", "").trim(),
                            phonetic = obj.optString("phonetic", "").trim(),
                            partOfSpeech = obj.optString("partOfSpeech", "word").trim(),
                            definition = obj.optString("definition", "").trim(),
                            example = obj.optString("exampleSentence", "").trim(),
                            synonyms = syns,
                            antonyms = ants
                        )
                        list.add(item)
                        map[key] = item
                    }
                } catch (e: Exception) {
                    Log.w("GlobalDictRepo", "Failed to load words_data.json bonus lexicon: ${e.message}")
                }

                cachedLexicon = list
                cachedEngMap = map
                list to map
            }
        }

    suspend fun search(query: String, direction: SearchDirection): GlobalSearchResult {
        val clean = query.trim()
        if (clean.isBlank()) {
            return GlobalSearchResult(entries = emptyList(), suggestions = emptyList())
        }

        val geoIndex = ensureGeoDatabaseLoaded()
        val (lexiconList, engMap) = ensureLexiconLoaded()

        return when (direction) {
            SearchDirection.ENG_TO_UZB -> searchEngToUzb(clean, geoIndex, lexiconList, engMap)
            SearchDirection.UZB_TO_ENG -> searchUzbToEng(clean, geoIndex, lexiconList, engMap)
        }
    }

    private fun lookupGeoEntities(
        query: String,
        direction: SearchDirection,
        geoIndex: GeoDatabaseIndex
    ): List<GeographicEntityInfo> {
        val norm = normalizeTextKey(query)
        val loose = stripApostrophesAndPunct(norm)
        return when (direction) {
            SearchDirection.ENG_TO_UZB -> {
                geoIndex.engExactMap[norm]
                    ?: geoIndex.engLooseMap[loose]
                    ?: emptyList()
            }
            SearchDirection.UZB_TO_ENG -> {
                geoIndex.uzbExactMap[norm]
                    ?: geoIndex.uzbLooseMap[loose]
                    ?: emptyList()
            }
        }
    }

    private fun buildGeoSearchResult(
        query: String,
        direction: SearchDirection,
        matches: List<GeographicEntityInfo>,
        geoIndex: GeoDatabaseIndex
    ): GlobalSearchResult {
        val entries = matches.map { geo ->
            GlobalDictionaryEntry(
                headword = geo.nameEn,
                matchedFromUzbek = if (direction == SearchDirection.UZB_TO_ENG) query else null,
                primaryUzbekTranslation = geo.nameUz,
                sourceLabel = "Tier 1 • Geographic Entities Database",
                direction = direction,
                tier = SearchResultTier.TIER_1_GEOGRAPHIC,
                geoInfo = geo
            )
        }

        val primaryGeo = matches.first()
        val relatedNames = linkedSetOf<String>()
        if (direction == SearchDirection.ENG_TO_UZB) {
            if (primaryGeo.capitalEn.isNotBlank() && !primaryGeo.capitalEn.equals(primaryGeo.nameEn, ignoreCase = true)) {
                relatedNames.add(primaryGeo.capitalEn)
            }
            if (primaryGeo.countryEn.isNotBlank() && !primaryGeo.countryEn.equals(primaryGeo.nameEn, ignoreCase = true)) {
                relatedNames.add(primaryGeo.countryEn)
            }
            geoIndex.allEntities
                .filter {
                    it.countryEn.equals(primaryGeo.countryEn, ignoreCase = true) &&
                        !it.nameEn.equals(primaryGeo.nameEn, ignoreCase = true)
                }
                .take(4)
                .forEach { relatedNames.add(it.nameEn) }
        } else {
            if (primaryGeo.capitalUz.isNotBlank() && !primaryGeo.capitalUz.equals(primaryGeo.nameUz, ignoreCase = true)) {
                relatedNames.add(primaryGeo.capitalUz)
            }
            if (primaryGeo.countryUz.isNotBlank() && !primaryGeo.countryUz.equals(primaryGeo.nameUz, ignoreCase = true)) {
                relatedNames.add(primaryGeo.countryUz)
            }
            geoIndex.allEntities
                .filter {
                    it.countryUz.equals(primaryGeo.countryUz, ignoreCase = true) &&
                        !it.nameUz.equals(primaryGeo.nameUz, ignoreCase = true)
                }
                .take(4)
                .forEach { relatedNames.add(it.nameUz) }
        }

        return GlobalSearchResult(
            entries = entries,
            suggestions = relatedNames.take(5)
        )
    }

    private suspend fun searchEngToUzb(
        query: String,
        geoIndex: GeoDatabaseIndex,
        lexiconList: List<OfflineLexiconItem>,
        engMap: Map<String, OfflineLexiconItem>
    ): GlobalSearchResult = coroutineScope {
        val lower = query.trim().lowercase(Locale.ROOT)

        // Strict Uzbek rejection in ENG -> UZB mode (including slang collisions like "biz", "salom", "hayr", etc.)
        if (isBlockedUzbekQueryInEnglishMode(lower)) {
            return@coroutineScope GlobalSearchResult(
                entries = emptyList(),
                suggestions = emptyList(),
                errorMessage = "Error: '$query' was not found in the English dictionary."
            )
        }

        // TIER 1: Geographic Entities Database (Countries, Capitals & Cities)
        val geoMatches = lookupGeoEntities(query, SearchDirection.ENG_TO_UZB, geoIndex)
        if (geoMatches.isNotEmpty()) {
            return@coroutineScope buildGeoSearchResult(
                query = query,
                direction = SearchDirection.ENG_TO_UZB,
                matches = geoMatches,
                geoIndex = geoIndex
            )
        }

        // Step 1: Validate online via Free Dictionary API (and check local 3,600 words / lexicon as a fast bonus)
        val offlineMatch = engMap[lower]
        val wordCount = lower.split(Regex("\\s+")).count { it.isNotBlank() }

        val freeDict = if (wordCount <= 3) service.fetchFreeDictionary(lower) else null

        // Step 2: If the word does NOT exist in English (404 Not Found and not in local 3,600 words), STOP immediately
        val hasLexicalMatch = freeDict != null || offlineMatch != null
        if (!hasLexicalMatch) {
            return@coroutineScope GlobalSearchResult(
                entries = emptyList(),
                suggestions = emptyList(),
                errorMessage = "Error: '$query' was not found in the English dictionary."
            )
        }

        // Step 3: Confirmed valid English word -> fetch accurate Uzbek translation online via Lingva / Google endpoint
        val dmSynDeferred = async {
            if (wordCount <= 2) service.fetchDatamuseSynonyms(lower) else emptyList()
        }
        val dmAntDeferred = async {
            if (wordCount <= 2) service.fetchDatamuseAntonyms(lower) else emptyList()
        }
        val bilingualDeferred = async { service.fetchBilingualTranslation(query, "en", "uz") }

        val dmSyns = dmSynDeferred.await()
        val dmAnts = dmAntDeferred.await()
        val bilingual = bilingualDeferred.await()

        // Tier 2 Entry Construction
        val uzbekGroups = mutableListOf<UzbekMeaningGroup>()
        var primaryUzbek = ""

        if (bilingual != null) {
            val rawPrimary = bilingual.primaryTranslation.trim()
            if (rawPrimary.isNotBlank() && !rawPrimary.equals(lower, ignoreCase = true)) {
                primaryUzbek = rawPrimary
            }
            uzbekGroups.addAll(bilingual.meaningsByPos)
        }

        if (offlineMatch != null && offlineMatch.translation.isNotBlank()) {
            if (primaryUzbek.isBlank()) {
                primaryUzbek = offlineMatch.translation
            } else if (!primaryUzbek.contains(offlineMatch.translation, ignoreCase = true)) {
                uzbekGroups.add(
                    0,
                    UzbekMeaningGroup(
                        partOfSpeech = offlineMatch.partOfSpeech.ifBlank { "lug'at" },
                        terms = offlineMatch.translation.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    )
                )
            }
        }

        val firstEnglishDef = freeDict?.meanings?.firstOrNull()?.definitions?.firstOrNull()?.definition
            ?: offlineMatch?.definition
        if (primaryUzbek.isBlank() && !firstEnglishDef.isNullOrBlank()) {
            val translatedDef = service.fetchUniversalTranslation(firstEnglishDef, "en", "uz")?.first
            if (translatedDef != null && translatedDef.primaryTranslation.isNotBlank()) {
                primaryUzbek = translatedDef.primaryTranslation
            }
        }

        val mergedMeanings = when {
            freeDict != null && freeDict.meanings.isNotEmpty() -> freeDict.meanings
            offlineMatch != null && offlineMatch.definition.isNotBlank() -> listOf(
                DictionaryMeaningGroup(
                    partOfSpeech = offlineMatch.partOfSpeech.ifBlank { "word" },
                    definitions = listOf(
                        DictionaryDefinitionItem(
                            definition = offlineMatch.definition,
                            example = offlineMatch.example
                        )
                    )
                )
            )
            else -> emptyList()
        }

        val headword = freeDict?.word?.takeIf { it.isNotBlank() } ?: offlineMatch?.word ?: cleanCandidateCase(query)
        val mergedSynonyms = buildList {
            freeDict?.synonyms?.let { addAll(it) }
            addAll(dmSyns)
            offlineMatch?.synonyms?.let { addAll(it) }
        }.filter { it.isNotBlank() && !it.equals(headword, ignoreCase = true) }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .take(14)

        val mergedAntonyms = buildList {
            freeDict?.antonyms?.let { addAll(it) }
            addAll(dmAnts)
            offlineMatch?.antonyms?.let { addAll(it) }
        }.filter { it.isNotBlank() && !it.equals(headword, ignoreCase = true) }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .take(14)

        val sources = buildList {
            if (freeDict != null) add("Free Dictionary API")
            if (dmSyns.isNotEmpty() || dmAnts.isNotEmpty()) add("Datamuse")
            if (bilingual != null) add("Lingva / Google UZB")
            if (offlineMatch != null) add("Global Lexicon")
        }.joinToString(" • ").ifBlank { "Global Dictionary" }

        val entry = GlobalDictionaryEntry(
            headword = headword,
            phonetic = freeDict?.phonetic?.ifBlank { offlineMatch?.phonetic ?: "" } ?: (offlineMatch?.phonetic ?: ""),
            audioUrl = freeDict?.audioUrl,
            audioUrlUs = freeDict?.audioUrlUs,
            audioUrlUk = freeDict?.audioUrlUk,
            primaryUzbekTranslation = primaryUzbek,
            uzbekMeanings = uzbekGroups,
            meanings = mergedMeanings,
            synonyms = mergedSynonyms,
            antonyms = mergedAntonyms,
            sourceLabel = "Tier 2 • $sources",
            direction = SearchDirection.ENG_TO_UZB,
            tier = SearchResultTier.TIER_2_DICTIONARY_THESAURUS
        )

        val relatedPrefixSuggestions = lexiconList
            .filter {
                !it.word.equals(headword, ignoreCase = true) &&
                    it.word.startsWith(lower, ignoreCase = true)
            }
            .take(5)
            .map { it.word }

        GlobalSearchResult(
            entries = listOf(entry),
            suggestions = relatedPrefixSuggestions
        )
    }

    private suspend fun searchUzbToEng(
        query: String,
        geoIndex: GeoDatabaseIndex,
        lexiconList: List<OfflineLexiconItem>,
        engMap: Map<String, OfflineLexiconItem>
    ): GlobalSearchResult = coroutineScope {
        // TIER 1: Geographic Entities Database (Countries, Capitals & Cities)
        val geoMatches = lookupGeoEntities(query, SearchDirection.UZB_TO_ENG, geoIndex)
        if (geoMatches.isNotEmpty()) {
            return@coroutineScope buildGeoSearchResult(
                query = query,
                direction = SearchDirection.UZB_TO_ENG,
                matches = geoMatches,
                geoIndex = geoIndex
            )
        }

        // TIER 2 & 3: Online Uzbek -> English Translation via Lingva / Google + Exact Local Lexicon
        val normalizedQuery = normalizeTextKey(query)
        val strippedQuery = stripApostrophesAndPunct(normalizedQuery)

        val exactLexiconMatches = lexiconList.filter { item ->
            item.translation.split(",", ";")
                .map { normalizeTextKey(it) }
                .any {
                    it == normalizedQuery ||
                        (strippedQuery.isNotBlank() && stripApostrophesAndPunct(it) == strippedQuery)
                }
        }

        val universalTrans = service.fetchUniversalTranslation(query, "uz", "en")
        val bilingual = universalTrans?.first
        val translatorProvider = universalTrans?.second ?: "Lingva / Google Translate"

        val candidateEnglishWords = linkedSetOf<String>()

        if (bilingual != null) {
            val primary = bilingual.primaryTranslation.trim()
            if (isValidEnglishCandidate(primary, normalizedQuery)) {
                candidateEnglishWords.add(primary)
            }
            bilingual.allTerms.forEach { term ->
                if (isValidEnglishCandidate(term, normalizedQuery)) {
                    candidateEnglishWords.add(term)
                }
            }
        }

        exactLexiconMatches.forEach { candidateEnglishWords.add(it.word) }

        val isSameWordBack = bilingual != null &&
            bilingual.primaryTranslation.isNotBlank() &&
            bilingual.primaryTranslation.trim().equals(query.trim(), ignoreCase = true)

        val topCandidates = candidateEnglishWords.take(4).toList()
        if (topCandidates.isEmpty() || (isSameWordBack && exactLexiconMatches.isEmpty())) {
            return@coroutineScope GlobalSearchResult(
                entries = emptyList(),
                suggestions = emptyList(),
                errorMessage = "Xatolik: '$query' so'zining tarjimasi topilmadi."
            )
        }

        val resolvedEntries = topCandidates.map { englishCandidate ->
            async {
                val lookupKey = englishCandidate.trim().lowercase(Locale.ROOT)
                val wordCount = lookupKey.split(Regex("\\s+")).count { it.isNotBlank() }
                val offlineItem = engMap[lookupKey]

                val freeDictDeferred = async {
                    if (wordCount <= 3) service.fetchFreeDictionary(lookupKey) else null
                }
                val dmSynDeferred = async {
                    if (wordCount <= 2) service.fetchDatamuseSynonyms(lookupKey) else emptyList()
                }
                val dmAntDeferred = async {
                    if (wordCount <= 2) service.fetchDatamuseAntonyms(lookupKey) else emptyList()
                }
                val reverseUzbDeferred = async {
                    if (wordCount <= 3) service.fetchBilingualTranslation(lookupKey, "en", "uz") else null
                }

                val freeDict = freeDictDeferred.await()
                val dmSyns = dmSynDeferred.await()
                val dmAnts = dmAntDeferred.await()
                val reverseUzb = reverseUzbDeferred.await()

                val headword = freeDict?.word?.takeIf { it.isNotBlank() }
                    ?: offlineItem?.word
                    ?: englishCandidate

                val meanings = when {
                    freeDict != null && freeDict.meanings.isNotEmpty() -> freeDict.meanings
                    offlineItem != null && offlineItem.definition.isNotBlank() -> listOf(
                        DictionaryMeaningGroup(
                            partOfSpeech = offlineItem.partOfSpeech.ifBlank { "word" },
                            definitions = listOf(
                                DictionaryDefinitionItem(
                                    definition = offlineItem.definition,
                                    example = offlineItem.example
                                )
                            )
                        )
                    )
                    else -> emptyList()
                }

                val mergedSyns = buildList {
                    freeDict?.synonyms?.let { addAll(it) }
                    addAll(dmSyns)
                    offlineItem?.synonyms?.let { addAll(it) }
                }.filter { it.isNotBlank() && !it.equals(headword, ignoreCase = true) }
                    .distinctBy { it.lowercase(Locale.ROOT) }
                    .take(12)

                val mergedAnts = buildList {
                    freeDict?.antonyms?.let { addAll(it) }
                    addAll(dmAnts)
                    offlineItem?.antonyms?.let { addAll(it) }
                }.filter { it.isNotBlank() && !it.equals(headword, ignoreCase = true) }
                    .distinctBy { it.lowercase(Locale.ROOT) }
                    .take(12)

                val uzbMeaning = offlineItem?.translation?.takeIf { it.isNotBlank() }
                    ?: reverseUzb?.primaryTranslation?.takeIf {
                        it.isNotBlank() && !it.equals(headword, ignoreCase = true)
                    }
                    ?: query

                val uzbGroups = reverseUzb?.meaningsByPos ?: emptyList()

                val isTier2Lexical = freeDict != null || offlineItem != null || meanings.isNotEmpty()
                val entryTier = if (isTier2Lexical) {
                    SearchResultTier.TIER_2_DICTIONARY_THESAURUS
                } else {
                    SearchResultTier.TIER_3_TRANSLATION_FALLBACK
                }

                val sources = buildList {
                    add(if (isTier2Lexical) "Tier 2" else "Tier 3")
                    add("UZB ➔ ENG")
                    if (freeDict != null) add("Free Dictionary API")
                    if (dmSyns.isNotEmpty() || dmAnts.isNotEmpty()) add("Datamuse")
                    if (offlineItem != null) add("Global Lexicon")
                    if (!isTier2Lexical) add(translatorProvider)
                }.joinToString(" • ")

                GlobalDictionaryEntry(
                    headword = headword,
                    matchedFromUzbek = query,
                    phonetic = freeDict?.phonetic?.ifBlank { offlineItem?.phonetic ?: "" }
                        ?: (offlineItem?.phonetic ?: ""),
                    audioUrl = freeDict?.audioUrl,
                    audioUrlUs = freeDict?.audioUrlUs,
                    audioUrlUk = freeDict?.audioUrlUk,
                    primaryUzbekTranslation = uzbMeaning,
                    uzbekMeanings = uzbGroups,
                    meanings = meanings,
                    synonyms = mergedSyns,
                    antonyms = mergedAnts,
                    sourceLabel = sources,
                    direction = SearchDirection.UZB_TO_ENG,
                    tier = entryTier
                )
            }
        }.awaitAll()

        GlobalSearchResult(
            entries = resolvedEntries.distinctBy { "${it.headword.lowercase(Locale.ROOT)}_${it.primaryUzbekTranslation.lowercase(Locale.ROOT)}" },
            suggestions = emptyList()
        )
    }

    private fun isBlockedUzbekQueryInEnglishMode(lower: String): Boolean {
        if (lower.contains("o'") || lower.contains("g'") ||
            lower.contains("o‘") || lower.contains("g‘") ||
            lower.contains("oʻ") || lower.contains("gʻ") ||
            lower.contains("ʻ") || lower.contains("’") || lower.contains("‘")
        ) {
            return true
        }
        return lower in BLOCKED_UZBEK_COLLISION_WORDS
    }

    private fun jsonStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val value = arr.optString(i).trim()
                if (value.isNotBlank()) add(value)
            }
        }
    }

    private fun normalizeTextKey(text: String): String {
        return text.trim()
            .lowercase(Locale.ROOT)
            .replace("ʻ", "'")
            .replace("’", "'")
            .replace("‘", "'")
            .replace("`", "'")
            .replace(Regex("\\s+"), " ")
    }

    private fun stripApostrophesAndPunct(normalizedText: String): String {
        return normalizedText
            .replace("'", "")
            .replace("-", " ")
            .replace(".", "")
            .replace(",", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isValidEnglishCandidate(candidate: String, normalizedUzbekQuery: String): Boolean {
        val trimmed = candidate.trim()
        if (trimmed.isBlank()) return false
        val normalizedCandidate = normalizeTextKey(trimmed)
        if (normalizedCandidate == normalizedUzbekQuery) return false
        if (stripApostrophesAndPunct(normalizedCandidate) == stripApostrophesAndPunct(normalizedUzbekQuery)) {
            return false
        }
        return trimmed.any { it in 'a'..'z' || it in 'A'..'Z' }
    }

    private fun cleanCandidateCase(input: String): String {
        return input.trim()
    }

    // --- Independent Global Recent Searches (SharedPreferences, last 5 successful lookups) ---

    companion object {
        const val MAX_RECENT_SEARCHES = 5
        private val BLOCKED_UZBEK_COLLISION_WORDS = setOf(
            "biz", "salom", "hayr", "xayr", "kitob", "uy", "men", "siz", "sen", "ular",
            "non", "suv", "bola", "ona", "ota", "rahmat", "yaxshi", "yomon", "katta",
            "kichik", "bugun", "ertaga", "kecha", "maktab", "shahar", "ovqat", "choy",
            "dost", "soz", "yoq", "uzbek", "ozbek"
        )
    }

    fun getRecentSearches(): List<String> {
        val raw = prefs.getString("recent_searches_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i).trim()
                    if (s.isNotBlank()) add(s)
                }
            }.take(MAX_RECENT_SEARCHES)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getRecentSearchDirection(query: String): SearchDirection? {
        val cleanKey = query.trim().lowercase(Locale.ROOT)
        if (cleanKey.isBlank()) return null
        val raw = prefs.getString("recent_search_directions_json", null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val dirName = obj.optString(cleanKey, "")
            SearchDirection.entries.firstOrNull { it.name == dirName }
        } catch (_: Exception) {
            null
        }
    }

    fun recordRecentSearch(query: String, direction: SearchDirection? = null): List<String> {
        val clean = query.trim()
        if (clean.isBlank()) return getRecentSearches()
        val updated = buildList {
            add(clean)
            getRecentSearches().forEach { existing ->
                if (!existing.equals(clean, ignoreCase = true)) {
                    add(existing)
                }
            }
        }.take(MAX_RECENT_SEARCHES)
        saveRecentSearches(updated)
        if (direction != null) {
            saveRecentSearchDirection(clean, direction)
        }
        return updated
    }

    fun deleteRecentSearch(query: String): List<String> {
        val updated = getRecentSearches()
            .filterNot { it.equals(query.trim(), ignoreCase = true) }
            .take(MAX_RECENT_SEARCHES)
        saveRecentSearches(updated)
        removeRecentSearchDirection(query)
        return updated
    }

    fun clearRecentSearches(): List<String> {
        prefs.edit()
            .remove("recent_searches_json")
            .remove("recent_search_directions_json")
            .apply()
        return emptyList()
    }

    private fun saveRecentSearches(items: List<String>) {
        val arr = JSONArray()
        items.take(MAX_RECENT_SEARCHES).forEach { arr.put(it) }
        prefs.edit().putString("recent_searches_json", arr.toString()).apply()
    }

    private fun saveRecentSearchDirection(query: String, direction: SearchDirection) {
        val cleanKey = query.trim().lowercase(Locale.ROOT)
        if (cleanKey.isBlank()) return
        val obj = try {
            val raw = prefs.getString("recent_search_directions_json", null)
            if (raw != null) JSONObject(raw) else JSONObject()
        } catch (_: Exception) {
            JSONObject()
        }
        obj.put(cleanKey, direction.name)
        prefs.edit().putString("recent_search_directions_json", obj.toString()).apply()
    }

    private fun removeRecentSearchDirection(query: String) {
        val cleanKey = query.trim().lowercase(Locale.ROOT)
        if (cleanKey.isBlank()) return
        val raw = prefs.getString("recent_search_directions_json", null) ?: return
        try {
            val obj = JSONObject(raw)
            obj.remove(cleanKey)
            prefs.edit().putString("recent_search_directions_json", obj.toString()).apply()
        } catch (_: Exception) {
        }
    }
}
