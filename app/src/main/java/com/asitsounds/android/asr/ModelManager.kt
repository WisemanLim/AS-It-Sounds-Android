package com.asitsounds.android.asr

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.security.MessageDigest
import java.util.UUID

class ModelManager(private val context: Context) {

    fun modelsDir(): File = File(context.noBackupFilesDir, "models").also { it.mkdirs() }

    fun modelFile(model: WhisperModel): File = File(modelsDir(), model.filename)

    fun isCached(model: WhisperModel): Boolean {
        val f = modelFile(model)
        return f.exists() && f.length() > 0L
    }

    fun isVerified(model: WhisperModel): Boolean {
        val f = modelFile(model)
        return f.exists() && sha256(f) == model.sha256
    }

    fun enqueueDownload(model: WhisperModel): UUID {
        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(
                workDataOf(
                    ModelDownloadWorker.KEY_MODEL_ID  to model.id,
                    ModelDownloadWorker.KEY_URL       to WhisperModel.downloadUrl(model),
                    ModelDownloadWorker.KEY_DEST_PATH to modelFile(model).absolutePath,
                    ModelDownloadWorker.KEY_SHA256    to model.sha256
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("model_download_${model.id}")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "model_download_${model.id}",
            ExistingWorkPolicy.KEEP,
            request
        )
        return request.id
    }

    fun downloadState(model: WhisperModel): Flow<WorkInfo?> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow("model_download_${model.id}")
            .map { it.firstOrNull() }

    companion object {
        internal fun sha256(file: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { stream ->
                val buf = ByteArray(8192)
                var n: Int
                while (stream.read(buf).also { n = it } != -1) {
                    digest.update(buf, 0, n)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
