package com.asitsounds.android.platform

import android.content.Context
import androidx.work.*
import com.asitsounds.android.pipeline.TranscriptionWorker
import java.util.UUID

// Enqueues background transcription as unique work per file (resume-safe via checkpoint).
class TranscriptionScheduler(private val context: Context) {

    fun enqueue(fileId: Long, uri: String, modelPath: String, language: String = ""): UUID {
        val request = OneTimeWorkRequestBuilder<TranscriptionWorker>()
            .setInputData(
                workDataOf(
                    TranscriptionWorker.KEY_FILE_ID    to fileId,
                    TranscriptionWorker.KEY_URI        to uri,
                    TranscriptionWorker.KEY_MODEL_PATH to modelPath,
                    TranscriptionWorker.KEY_LANGUAGE   to language
                )
            )
            .setConstraints(Constraints.Builder().build())
            .addTag(TranscriptionWorker.workName(fileId))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            TranscriptionWorker.workName(fileId),
            ExistingWorkPolicy.KEEP,
            request
        )
        return request.id
    }

    fun cancel(fileId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(TranscriptionWorker.workName(fileId))
    }
}
