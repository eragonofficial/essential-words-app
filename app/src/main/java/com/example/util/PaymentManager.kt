package com.example.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Isolated, safe Mock Payment & Subscription Manager.
 *
 * Keeps all billing, purchase, and premium subscription logic decoupled and mocked
 * so that app builds, emulator tests, and CI/CD runs never break due to external
 * Google Play Billing client dependencies or sandbox errors.
 */
object PaymentManager {
    private const val PREFS_NAME = "payment_prefs"
    private const val KEY_IS_PREMIUM = "is_premium_unlocked"

    private val _isPremiumUnlocked = MutableStateFlow(false)
    val isPremiumUnlocked: StateFlow<Boolean> = _isPremiumUnlocked.asStateFlow()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isPremiumUnlocked.value = prefs.getBoolean(KEY_IS_PREMIUM, false)
    }

    /**
     * Mock purchase function for testing premium flows safely.
     */
    fun mockPurchasePremium(context: Context, onResult: (Boolean) -> Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_PREMIUM, true).apply()
        _isPremiumUnlocked.value = true
        onResult(true)
    }

    /**
     * Mock restore purchases function.
     */
    fun mockRestorePurchases(context: Context, onResult: (Boolean) -> Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val wasPremium = prefs.getBoolean(KEY_IS_PREMIUM, false)
        _isPremiumUnlocked.value = wasPremium
        onResult(wasPremium)
    }

    /**
     * Reset subscription state for testing.
     */
    fun resetForTesting(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_IS_PREMIUM).apply()
        _isPremiumUnlocked.value = false
    }
}
