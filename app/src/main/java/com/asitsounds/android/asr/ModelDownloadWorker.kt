package com.asitsounds.android.asr

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class ModelDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    companion object {
        const val KEY_MODEL_ID  = "model_id"
        const val KEY_URL       = "url"
        const val KEY_DEST_PATH = "dest_path"
        const val KEY_SHA256    = "sha256"
        const val KEY_PROGRESS  = "progress"
        private const val NOTIF_ID = 1001
        const val CHANNEL_ID = "model_download"
    }

    override suspend fun doWork(): Result {
        val url      = inputData.getString(KEY_URL)       ?: return Result.failure()
        val destPath = inputData.getString(KEY_DEST_PATH) ?: return Result.failure()
        val expected = inputData.getString(KEY_SHA256)    ?: return Result.failure()

        val destFile = File(destPath)
        destFile.parentFile?.mkdirs()

        setForeground(createForegroundInfo(0))

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.retry()
                    val body = response.body ?: return@withContext Result.retry()
                    val total = body.contentLength()
                    var downloaded = 0L
                    val digest = MessageDigest.getInstance("SHA-256")
                    val tmp = File("$destPath.tmp")
                    tmp.outputStream().use { out ->
                        body.byteStream().use { inp ->
                            val buf = ByteArray(8192)
                            var n: Int
                            while (inp.read(buf).also { n = it } != -1) {
                                out.write(buf, 0, n)
                                digest.update(buf, 0, n)
                                downloaded += n
                                if (total > 0) {
                                    val pct = (downloaded * 100 / total).toInt()
                                    setProgress(workDataOf(KEY_PROGRESS to pct))
                                    setForeground(createForegroundInfo(pct))
                                }
                            }
                        }
                    }
                    val actual = digest.digest().joinToString("") { "%02x".format(it) }
                    if (actual != expected) {
                        tmp.delete()
                        return@withContext Result.failure(workDataOf("error" to "SHA256 mismatch"))
                    }
                    tmp.renameTo(destFile)
                }
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading Whisper model")
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .build()
        return ForegroundInfo(NOTIF_ID, notif)
    }
}
