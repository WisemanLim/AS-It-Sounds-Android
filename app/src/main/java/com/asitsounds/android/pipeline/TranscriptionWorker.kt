package com.asitsounds.android.pipeline

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.asitsounds.android.translate.NoOpTranslator

class TranscriptionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_FILE_ID    = "file_id"
        const val KEY_URI        = "uri"
        const val KEY_LANGUAGE   = "language"
        const val KEY_MODEL_PATH = "model_path"
        const val KEY_PROGRESS   = "progress"
        const val WORK_NAME_PREFIX = "transcribe_"

        fun workName(fileId: Long) = "$WORK_NAME_PREFIX$fileId"
    }

    override suspend fun doWork(): Result {
        val fileId    = inputData.getLong(KEY_FILE_ID, -1L).takeIf { it >= 0 } ?: return Result.failure()
        val uriStr    = inputData.getString(KEY_URI)        ?: return Result.failure()
        val modelPath = inputData.getString(KEY_MODEL_PATH) ?: return Result.failure()
        val language  = inputData.getString(KEY_LANGUAGE) ?: ""

        val engine = WhisperJniEngine(modelPath)
        val orchestrator = PipelineOrchestrator(
            audioSource   = DefaultAudioSource(applicationContext, Uri.parse(uriStr)),
            whisperEngine = engine,
            translator    = NoOpTranslator,     // replaced in E6
            checkpointDir = applicationContext.filesDir
        )

        return try {
            orchestrator.process(fileId, language)
                .collect { event ->
                    when (event) {
                        is PipelineEvent.AsrProgress ->
                            setProgress(workDataOf(KEY_PROGRESS to event.progressPct))
                        is PipelineEvent.Error -> throw event.cause
                        else -> Unit
                    }
                }
            engine.close()
            Result.success()
        } catch (e: Exception) {
            engine.close()
            Result.retry()
        }
    }
}
