package com.example.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object StreakManager {
    private const val PREFS_NAME = "vocab_streak_prefs"
    private const val KEY_STREAK = "key_streak_count"
    private const val KEY_LAST_DATE = "key_last_completed_date"

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun getStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_DATE, null) ?: return 0
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        return if (lastDate == today || lastDate == yesterday) {
            prefs.getInt(KEY_STREAK, 0)
        } else {
            // Streak broken
            0
        }
    }

    fun recordUnitCompletionToday(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        val currentStreak = prefs.getInt(KEY_STREAK, 0)

        val newStreak = when (lastDate) {
            today -> {
                // Already completed something today, maintain streak
                currentStreak.coerceAtLeast(1)
            }
            yesterday -> {
                // Completed yesterday, streak increments!
                currentStreak + 1
            }
            else -> {
                // Streak restarted or first day
                1
            }
        }

        prefs.edit()
            .putInt(KEY_STREAK, newStreak)
            .putString(KEY_LAST_DATE, today)
            .apply()

        return newStreak
    }

    fun getMotivationBadge(streak: Int): Pair<String, String> {
        return when {
            streak <= 0 -> Pair("Start Streak Today", "Complete any unit quiz to ignite your daily learning streak! 🚀")
            streak in 1..2 -> Pair("Starter Spark ⚡", "$streak day streak! Excellent momentum, keep it burning!")
            streak in 3..6 -> Pair("Consistent Scholar 🔥", "$streak days in a row! Your vocabulary retention is soaring!")
            streak in 7..13 -> Pair("Word Master 🏆", "$streak day streak! One full week of dedication, incredible!")
            else -> Pair("Unstoppable Legend 👑", "$streak day streak! You are mastering all 4000 words!")
        }
    }
}
