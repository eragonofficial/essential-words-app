package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import java.net.URLEncoder

class AudioPronunciationPlayer(context: Context) {

    private val ttsHelper = TextToSpeechHelper(context.applicationContext)
    private var mediaPlayer: MediaPlayer? = null

    fun play(word: String, audioUrl: String?, isUk: Boolean = false) {
        val cleanWord = word.trim()
        if (cleanWord.isBlank()) return

        if (audioUrl.isNullOrBlank()) {
            stopMediaPlayer()
            val spokeViaTts = ttsHelper.speak(cleanWord, isUk)
            if (!spokeViaTts) {
                playFallbackStream(cleanWord, isUk)
            }
            return
        }

        try {
            stopMediaPlayer()
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener { mp ->
                    mp.release()
                    if (mediaPlayer === mp) {
                        mediaPlayer = null
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    Log.w("AudioPlayer", "MediaPlayer error ($what/$extra) for $audioUrl, falling back to TTS")
                    mp.release()
                    if (mediaPlayer === mp) {
                        mediaPlayer = null
                    }
                    if (!ttsHelper.speak(cleanWord, isUk)) {
                        playFallbackStream(cleanWord, isUk)
                    }
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.w("AudioPlayer", "Audio playback failed for $audioUrl: ${e.message}")
            stopMediaPlayer()
            if (!ttsHelper.speak(cleanWord, isUk)) {
                playFallbackStream(cleanWord, isUk)
            }
        }
    }

    private fun playFallbackStream(word: String, isUk: Boolean) {
        try {
            val tl = if (isUk) "en-GB" else "en-US"
            val encoded = URLEncoder.encode(word, "UTF-8")
            val streamUrl = "https://translate.googleapis.com/translate_tts?ie=UTF-8&client=gtx&tl=$tl&q=$encoded"
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(streamUrl)
                setOnPreparedListener { mp -> mp.start() }
                setOnCompletionListener { mp ->
                    mp.release()
                    if (mediaPlayer === mp) mediaPlayer = null
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (mediaPlayer === mp) mediaPlayer = null
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (_: Exception) {
        }
    }

    fun playUs(word: String, audioUrlUs: String? = null) {
        play(word = word, audioUrl = audioUrlUs, isUk = false)
    }

    fun playUk(word: String, audioUrlUk: String? = null) {
        play(word = word, audioUrl = audioUrlUk, isUk = true)
    }

    private fun stopMediaPlayer() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }
    }

    fun release() {
        stopMediaPlayer()
        ttsHelper.shutdown()
    }
}
