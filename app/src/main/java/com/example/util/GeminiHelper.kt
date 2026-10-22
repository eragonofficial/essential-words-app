package com.example.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiHelper {
    private val generativeModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel("gemini-2.5-flash")
    }

    suspend fun generateQuizQuestion(word: String, definition: String): String = withContext(Dispatchers.IO) {
        try {
            val prompt = "Create a multiple-choice vocabulary quiz question for the English word '$word' which means '$definition'. Format the output clearly with the Question, 4 Options (A, B, C, D), and the Correct Answer."
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Quiz question generation unavailable."
        } catch (e: Exception) {
            Log.e("GeminiHelper", "Error generating quiz question", e)
            "Question: What is the meaning of '$word'?\nOptions:\nA) $definition\nB) Incorrect 1\nC) Incorrect 2\nD) Incorrect 3\nCorrect Answer: A"
        }
    }

    suspend fun explainWord(word: String, definition: String): String = withContext(Dispatchers.IO) {
        try {
            val prompt = "Provide a simple, engaging explanation and 2 extra example sentences for the English word '$word' (Definition: '$definition'). Include Uzbek translation context."
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Explanation unavailable."
        } catch (e: Exception) {
            Log.e("GeminiHelper", "Error explaining word", e)
            "Explanation for $word: $definition"
        }
    }
}
