package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.local.AppDatabase

class SpacedRepetitionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val learnedWords = db.wordDao().getAllWordsList().filter { it.isLearned && it.learnedTimestamp > 0 }
            val now = System.currentTimeMillis()

            val wordsDueForReview = learnedWords.filter { word ->
                val daysPassed = (now - word.learnedTimestamp) / (1000L * 60 * 60 * 24)
                daysPassed in 3..4 || daysPassed in 7..8 || daysPassed in 14..15
            }

            if (wordsDueForReview.isNotEmpty()) {
                val sampleWord = wordsDueForReview.first().word
                val count = wordsDueForReview.size
                sendReviewNotification(count, sampleWord)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendReviewNotification(count: Int, sampleWord: String) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "spaced_repetition_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Spaced Repetition Reviews",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to review learned words after 3, 7, and 14 days."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Spaced Repetition Review! 🧠")
            .setContentText("$count words are due for review today (e.g., '$sampleWord').")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "You have $count vocabulary words ready for spaced interval review (3, 7, or 14-day cycle) including '$sampleWord'. Tap to review and lock them into long-term memory!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2002, notification)
    }
}
