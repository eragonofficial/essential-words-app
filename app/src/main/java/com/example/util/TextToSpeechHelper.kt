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

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.language = currentLocale
        } else {
            Log.e("TTSHelper", "Failed to initialize TextToSpeech: status = $status")
        }
    }

    fun setAccent(isUk: Boolean) {
        val targetLocale = if (isUk) Locale.UK else Locale.US
        currentLocale = targetLocale
        if (isReady && tts != null) {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTSHelper", "Target locale $targetLocale not supported, falling back to English")
                tts?.setLanguage(Locale.ENGLISH)
            }
        }
    }

    fun speak(text: String, isUk: Boolean? = null) {
        val targetLocale = if (isUk != null) {
            if (isUk) Locale.UK else Locale.US
        } else {
            currentLocale
        }
        currentLocale = targetLocale
        if (isReady && tts != null) {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.ENGLISH)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "${System.currentTimeMillis()}_${text.hashCode()}")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
