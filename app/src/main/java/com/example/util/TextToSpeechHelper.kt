package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isReady: Boolean = false
        private set

    var currentLocale: Locale = Locale.US
        private set

    private var pendingSpeech: Pair<String, Boolean?>? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.language = currentLocale
            pendingSpeech?.let { (text, isUk) ->
                pendingSpeech = null
                speak(text, isUk)
            }
        } else {
            Log.e("TTSHelper", "Failed to initialize TextToSpeech: status = $status")
        }
    }

    fun setAccent(isUk: Boolean) {
        val targetLocale = if (isUk) Locale.UK else Locale.US
        currentLocale = targetLocale
        if (isReady && tts != null) {
            applyLocaleAndVoice(targetLocale, isUk)
        }
    }

    private fun applyLocaleAndVoice(targetLocale: Locale, isUk: Boolean) {
        val engine = tts ?: return
        val result = engine.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w("TTSHelper", "Target locale $targetLocale not supported, falling back to English")
            engine.setLanguage(Locale.ENGLISH)
        }
        try {
            val langTag = if (isUk) "en-gb" else "en-us"
            val countryCode = if (isUk) "GB" else "US"
            val matchingVoice = engine.voices?.firstOrNull { voice ->
                !voice.isNetworkConnectionRequired &&
                    (voice.locale.country.equals(countryCode, ignoreCase = true) ||
                        voice.name.lowercase(Locale.ROOT).contains(langTag) ||
                        voice.name.lowercase(Locale.ROOT).contains(if (isUk) "en_gb" else "en_us"))
            } ?: engine.voices?.firstOrNull { voice ->
                voice.locale.country.equals(countryCode, ignoreCase = true) ||
                    voice.name.lowercase(Locale.ROOT).contains(langTag) ||
                    voice.name.lowercase(Locale.ROOT).contains(if (isUk) "en_gb" else "en_us")
            }
            if (matchingVoice != null) {
                engine.voice = matchingVoice
            }
        } catch (_: Exception) {
        }
    }

    fun speak(text: String, isUk: Boolean? = null): Boolean {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return false
        val useUk = isUk ?: (currentLocale == Locale.UK)
        val targetLocale = if (useUk) Locale.UK else Locale.US
        currentLocale = targetLocale

        val engine = tts
        if (isReady && engine != null) {
            applyLocaleAndVoice(targetLocale, useUk)
            val res = engine.speak(
                cleanText,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "${System.currentTimeMillis()}_${cleanText.hashCode()}"
            )
            return res == TextToSpeech.SUCCESS
        } else {
            pendingSpeech = cleanText to useUk
            return false
        }
    }

    fun shutdown() {
        pendingSpeech = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
