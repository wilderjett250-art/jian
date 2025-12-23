package com.example.jian2.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "mojian_diary_reminder_2230"

    fun ensureScheduledIfEnabled(context: Context) {
        val sp = context.getSharedPreferences("mojian_prefs", 0)
        val enabled = sp.getBoolean("reminder_enabled", false)
        if (enabled) scheduleNext2230(context)
    }

    fun scheduleNext2230(context: Context) {
        val delay = millisUntilNext2230()
        val req = OneTimeWorkRequestBuilder<DiaryReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, req)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun millisUntilNext2230(): Long {
        val now = System.currentTimeMillis()

        val cal = Calendar.getInstance().apply {
            timeInMillis = now
        }

        val target = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 如果现在已经超过今天 22:30，则目标是明天 22:30
        if (target.timeInMillis <= cal.timeInMillis) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now
    }
}
