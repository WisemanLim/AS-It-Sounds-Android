package com.asitsounds.android.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    const val CHANNEL_TRANSCRIBE = "transcribe"
    const val NOTIF_ID_TRANSCRIBE = 2001

    // Progress text shown in the foreground-service notification.
    fun progressText(done: Int, total: Int): String =
        if (total <= 0) "준비 중…" else "변환 중 $done/$total 세그먼트"

    fun progressPercent(done: Int, total: Int): Int =
        if (total <= 0) 0 else ((done.toLong() * 100 / total).toInt()).coerceIn(0, 100)

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(NotificationManager::class.java)
            if (mgr.getNotificationChannel(CHANNEL_TRANSCRIBE) == null) {
                mgr.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_TRANSCRIBE,
                        "전사 진행률",
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
        }
    }

    fun buildProgress(context: Context, done: Int, total: Int): android.app.Notification =
        NotificationCompat.Builder(context, CHANNEL_TRANSCRIBE)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("As It Sounds")
            .setContentText(progressText(done, total))
            .setProgress(100, progressPercent(done, total), total <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
}
