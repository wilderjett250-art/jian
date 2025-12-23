package com.example.jian2.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jian2.R

class DiaryReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val sp = applicationContext.getSharedPreferences("mojian_prefs", 0)
        val enabled = sp.getBoolean("reminder_enabled", false)
        if (!enabled) return Result.success()

        showNotification()

        // 执行完后，继续排程下一次（每天 22:30）
        ReminderScheduler.scheduleNext2230(applicationContext)
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "mojian_reminder"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(
                channelId,
                "墨笺提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(ch)
        }

        val noti = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("墨笺提醒")
            .setContentText("今天写日记了吗？记录一下此刻的心情吧。")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(2230, noti)
    }
}
