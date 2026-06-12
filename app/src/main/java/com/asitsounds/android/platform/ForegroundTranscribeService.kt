package com.asitsounds.android.platform

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat

// Foreground service that keeps transcription alive + shows progress (iOS Live Activity 대응).
class ForegroundTranscribeService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val done = intent?.getIntExtra(EXTRA_DONE, 0) ?: 0
        val total = intent?.getIntExtra(EXTRA_TOTAL, 0) ?: 0
        NotificationHelper.ensureChannel(this)
        val notif = NotificationHelper.buildProgress(this, done, total)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this, NotificationHelper.NOTIF_ID_TRANSCRIBE, notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NotificationHelper.NOTIF_ID_TRANSCRIBE, notif)
        }
        return START_NOT_STICKY
    }

    companion object {
        const val EXTRA_DONE = "done"
        const val EXTRA_TOTAL = "total"

        fun start(context: Context, done: Int, total: Int) {
            val intent = Intent(context, ForegroundTranscribeService::class.java)
                .putExtra(EXTRA_DONE, done)
                .putExtra(EXTRA_TOTAL, total)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else
                context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ForegroundTranscribeService::class.java))
        }
    }
}
