package com.example.data.dictionary

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class FreeDictionaryApiResult(
    val word: String,
    val phonetic: String,
    val audioUrl: String?,
    val audioUrlUs: String?,
    val audioUrlUk: String?,
    val meanings: List<DictionaryMeaningGroup>,
    val synonyms: List<String>,
    val antonyms: List<String>
)

data class BilingualTranslationResult(
    val primaryTranslation: String,
    val meaningsByPos: List<UzbekMeaningGroup>,
    val allTerms: List<String>
)

data class DatamuseSuggestion(
    val word: String,
    val definitions: List<String>
)

class GlobalDictionaryService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun fetchFreeDictionary(word: String): FreeDictionaryApiResult? = withContext(Dispatchers.IO) {
        val clean = word.trim().lowercase()
        if (clean.isBlank()) return@withContext null

        val encoded = URLEncoder.encode(clean, "UTF-8").replace("+", "%20")
        val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$encoded"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val array = JSONArray(body)
                if (array.length() == 0) return@withContext null

                val firstEntry = array.getJSONObject(0)
                val headword = firstEntry.optString("word", clean)
                var bestPhonetic = firstEntry.optString("phonetic", "")
                var bestAudio: String? = null
                var audioUs: String? = null
                var audioUk: String? = null

                val phoneticsArr = firstEntry.optJSONArray("phonetics")
                if (phoneticsArr != null) {
                    for (i in 0 until phoneticsArr.length()) {
                        val pObj = phoneticsArr.getJSONObject(i)
                        val pText = pObj.optString("text", "")
                        val pAudio = pObj.optString("audio", "")
                        if (bestPhonetic.isBlank() && pText.isNotBlank()) {
                            bestPhonetic = pText
                        }
                        if (pAudio.isNotBlank()) {
                            val resolvedAudio = if (pAudio.startsWith("//")) "https:$pAudio" else pAudio
                            if (bestAudio.isNullOrBlank()) bestAudio = resolvedAudio
                            val lowerAudio = resolvedAudio.lowercase()
                            if (lowerAudio.contains("-us") || lowerAudio.contains("us.") || lowerAudio.contains("en_us")) {
                                if (audioUs.isNullOrBlank()) audioUs = resolvedAudio
                            } else if (lowerAudio.contains("-uk") || lowerAudio.contains("-gb") || lowerAudio.contains("uk.") || lowerAudio.contains("gb.") || lowerAudio.contains("en_gb")) {
                                if (audioUk.isNullOrBlank()) audioUk = resolvedAudio
                            }
                        }
                    }
                }

                val meaningsList = mutableListOf<DictionaryMeaningGroup>()
                val collectedSynonyms = linkedSetOf<String>()
                val collectedAntonyms = linkedSetOf<String>()

                for (entryIdx in 0 until array.length()) {
                    val entryObj = array.getJSONObject(entryIdx)
                    val meaningsArr = entryObj.optJSONArray("meanings") ?: continue
                    for (mIdx in 0 until meaningsArr.length()) {
                        val mObj = meaningsArr.getJSONObject(mIdx)
                        val pos = mObj.optString("partOfSpeech", "word")

                        mObj.optJSONArray("synonyms")?.let { synArr ->
                            for (s in 0 until synArr.length()) {
                                val valStr = synArr.optString(s).trim()
                                if (valStr.isNotBlank()) collectedSynonyms.add(valStr)
                            }
                        }
                        mObj.optJSONArray("antonyms")?.let { antArr ->
                            for (a in 0 until antArr.length()) {
                                val valStr = antArr.optString(a).trim()
                                if (valStr.isNotBlank()) collectedAntonyms.add(valStr)
                            }
                        }

                        val defsList = mutableListOf<DictionaryDefinitionItem>()
                        val defsArr = mObj.optJSONArray("definitions")
                        if (defsArr != null) {
                            for (dIdx in 0 until minOf(defsArr.length(), 4)) {
                                val dObj = defsArr.getJSONObject(dIdx)
                                val defText = dObj.optString("definition", "").trim()
                                val exText = dObj.optString("example", "").trim()
                                if (defText.isNotBlank()) {
                                    defsList.add(
                                        DictionaryDefinitionItem(
                                            definition = defText,
                                            example = exText
                                        )
                                    )
                                }
                                dObj.optJSONArray("synonyms")?.let { sArr ->
                                    for (s in 0 until sArr.length()) {
                                        val valStr = sArr.optString(s).trim()
                                        if (valStr.isNotBlank()) collectedSynonyms.add(valStr)
                                    }
                                }
                                dObj.optJSONArray("antonyms")?.let { aArr ->
                                    for (a in 0 until aArr.length()) {
                                        val valStr = aArr.optString(a).trim()
                                        if (valStr.isNotBlank()) collectedAntonyms.add(valStr)
                                    }
                                }
                            }
                        }

                        if (defsList.isNotEmpty()) {
                            meaningsList.add(DictionaryMeaningGroup(pos, defsList))
                        }
                    }
                }

                FreeDictionaryApiResult(
                    word = headword,
                    phonetic = bestPhonetic,
                    audioUrl = bestAudio,
                    audioUrlUs = audioUs,
                    audioUrlUk = audioUk,
                    meanings = meaningsList,
                    synonyms = collectedSynonyms.take(12),
                    antonyms = collectedAntonyms.take(12)
                )
            }
        } catch (e: Exception) {
            Log.w("GlobalDictService", "FreeDictionary error for '$clean': ${e.message}")
            null
        }
    }

    suspend fun fetchDatamuseSynonyms(word: String): List<String> = fetchDatamuseRelation(word, "rel_syn")

    suspend fun fetchDatamuseAntonyms(word: String): List<String> = fetchDatamuseRelation(word, "rel_ant")

    private suspend fun fetchDatamuseRelation(word: String, relationParam: String): List<String> = withContext(Dispatchers.IO) {
        val clean = word.trim().lowercase()
        if (clean.isBlank()) return@withContext emptyList()
        val encoded = URLEncoder.encode(clean, "UTF-8")
        val url = "https://api.datamuse.com/words?$relationParam=$encoded&max=12"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val array = JSONArray(body)
                val out = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    val w = array.getJSONObject(i).optString("word", "").trim()
                    if (w.isNotBlank() && !w.equals(clean, ignoreCase = true)) {
                        out.add(w)
                    }
                }
                out
            }
        } catch (e: Exception) {
            Log.w("GlobalDictService", "Datamuse $relationParam error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchDatamuseSuggestions(query: String): List<DatamuseSuggestion> = withContext(Dispatchers.IO) {
        val clean = query.trim().lowercase()
        if (clean.length < 2) return@withContext emptyList()
        val encoded = URLEncoder.encode("$clean*", "UTF-8")
        val url = "https://api.datamuse.com/words?sp=$encoded&md=d&max=8"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val array = JSONArray(body)
                val out = mutableListOf<DatamuseSuggestion>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val w = obj.optString("word", "").trim()
                    val defsArr = obj.optJSONArray("defs")
                    val defs = mutableListOf<String>()
                    if (defsArr != null) {
                        for (d in 0 until minOf(defsArr.length(), 3)) {
                            val rawDef = defsArr.optString(d, "")
                            // Datamuse defs format: "n\tdefinition text"
                            val cleanedDef = rawDef.substringAfter('\t', rawDef).trim()
                            if (cleanedDef.isNotBlank()) defs.add(cleanedDef)
                        }
                    }
                    if (w.isNotBlank()) {
                        out.add(DatamuseSuggestion(w, defs))
                    }
                }
                out
            }
        } catch (e: Exception) {
            Log.w("GlobalDictService", "Datamuse suggestions error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchBilingualTranslation(
        text: String,
        sourceLang: String,
        targetLang: String
    ): BilingualTranslationResult? = withContext(Dispatchers.IO) {
        val clean = text.trim()
        if (clean.isBlank()) return@withContext null
        val encodedPath = URLEncoder.encode(clean, "UTF-8").replace("+", "%20")
        val encodedQuery = URLEncoder.encode(clean, "UTF-8")

        // 1. Try Lingva Translate API (https://lingva.ml/api/v1/{sl}/{tl}/{query})
        try {
            val lingvaUrl = "https://lingva.ml/api/v1/$sourceLang/$targetLang/$encodedPath"
            val req = Request.Builder()
                .url(lingvaUrl)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val trans = json.optString("translation", "").trim()
                        if (trans.isNotBlank()) {
                            return@withContext BilingualTranslationResult(
                                primaryTranslation = trans,
                                meaningsByPos = emptyList(),
                                allTerms = listOf(trans)
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Fallback to Google Chrome Extension Translate Endpoint
        // (https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl={sl}&tl={tl}&q={query})
        try {
            val chromeUrl = "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=$sourceLang&tl=$targetLang&q=$encodedQuery"
            val req = Request.Builder()
                .url(chromeUrl)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string()?.trim()
                    if (!body.isNullOrBlank()) {
                        val trans = when {
                            body.startsWith("[") -> {
                                val arr = JSONArray(body)
                                if (arr.length() > 0) {
                                    val firstElem = arr.optJSONArray(0)
                                    if (firstElem != null && firstElem.length() > 0) {
                                        firstElem.optString(0, "").trim()
                                    } else {
                                        arr.optString(0, "").trim()
                                    }
                                } else ""
                            }
                            body.startsWith("{") -> {
                                val obj = JSONObject(body)
                                val directTrans = obj.optString("trans", "").trim()
                                if (directTrans.isNotBlank()) {
                                    directTrans
                                } else {
                                    val sentences = obj.optJSONArray("sentences")
                                    val sb = StringBuilder()
                                    if (sentences != null) {
                                        for (i in 0 until sentences.length()) {
                                            val sObj = sentences.optJSONObject(i)
                                            val chunk = sObj?.optString("trans", "") ?: ""
                                            sb.append(chunk)
                                        }
                                    }
                                    sb.toString().trim()
                                }
                            }
                            else -> body.trim('"')
                        }
                        if (trans.isNotBlank()) {
                            return@withContext BilingualTranslationResult(
                                primaryTranslation = trans,
                                meaningsByPos = emptyList(),
                                allTerms = listOf(trans)
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Fallback to Google GTX endpoint (includes dictionary meanings by POS when available)
        try {
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&dt=bd&q=$encodedQuery"
            val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val root = JSONArray(body)
                val primarySb = StringBuilder()
                val transArr = root.optJSONArray(0)
                if (transArr != null) {
                    for (i in 0 until transArr.length()) {
                        val seg = transArr.optJSONArray(i)
                        val chunk = seg?.optString(0, "") ?: ""
                        primarySb.append(chunk)
                    }
                }
                val primary = primarySb.toString().trim()
                val meaningsByPos = mutableListOf<UzbekMeaningGroup>()
                val allTerms = linkedSetOf<String>()
                if (primary.isNotBlank()) {
                    allTerms.add(primary)
                }

                val dictArr = root.optJSONArray(1)
                if (dictArr != null) {
                    for (i in 0 until dictArr.length()) {
                        val posEntry = dictArr.optJSONArray(i) ?: continue
                        val pos = posEntry.optString(0, "").trim()
                        val termsArr = posEntry.optJSONArray(1) ?: continue
                        val posTerms = mutableListOf<String>()
                        for (j in 0 until minOf(termsArr.length(), 6)) {
                            val t = termsArr.optString(j, "").trim()
                            if (t.isNotBlank()) {
                                posTerms.add(t)
                                allTerms.add(t)
                            }
                        }
                        if (posTerms.isNotEmpty()) {
                            meaningsByPos.add(UzbekMeaningGroup(partOfSpeech = pos, terms = posTerms))
                        }
                    }
                }

                if (primary.isNotBlank() || allTerms.isNotEmpty()) {
                    return@withContext BilingualTranslationResult(
                        primaryTranslation = primary.ifBlank { allTerms.first() },
                        meaningsByPos = meaningsByPos,
                        allTerms = allTerms.toList()
                    )
                }
            }
        } catch (_: Exception) {}

        return@withContext null
    }

    suspend fun fetchUniversalTranslation(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Pair<BilingualTranslationResult, String>? {
        val clean = text.trim()
        if (clean.isBlank()) return null

        val primary = fetchBilingualTranslation(clean, sourceLang, targetLang)
        if (primary != null && primary.primaryTranslation.isNotBlank()) {
            return primary to "Google Translate"
        }
        return null
    }
}

