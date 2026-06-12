package com.asitsounds.android.pipeline

import com.asitsounds.android.translit.TranslitEngine
import com.asitsounds.android.translate.TranslatorInterface
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class PipelineOrchestrator(
    private val audioSource: AudioSource,
    private val whisperEngine: WhisperEngine,
    private val translator: TranslatorInterface,
    private val checkpointDir: File? = null
) {

    fun process(
        fileId: Long,
        language: String = ""
    ): Flow<PipelineEvent> = flow {
        emit(PipelineEvent.Decoding)

        val samples = audioSource.decode()

        emit(PipelineEvent.AsrProgress(0))
        val rawSegments = whisperEngine.transcribe(samples, language)
        emit(PipelineEvent.AsrProgress(100))

        val startIdx = loadCheckpoint(fileId)

        rawSegments.forEachIndexed { idx, seg ->
            if (idx < startIdx) return@forEachIndexed

            emit(PipelineEvent.TranslatingSegment(idx))

            val (hangul, korean) = coroutineScope {
                val h = async { TranslitEngine.transliterate(seg.text) }
                val k = async { translator.translate(seg.text, seg.lang) }
                h.await() to k.await()
            }

            emit(
                PipelineEvent.Segment(
                    index      = idx,
                    startMs    = seg.startMs,
                    endMs      = seg.endMs,
                    textEn     = seg.text,
                    textHangul = hangul,
                    textKo     = korean,
                    lang       = seg.lang
                )
            )
            saveCheckpoint(fileId, idx + 1)
        }

        clearCheckpoint(fileId)
        emit(PipelineEvent.FileDone)
    }

    private fun checkpointFile(fileId: Long): File? =
        checkpointDir?.let { File(it, "cp_$fileId.txt") }

    private fun loadCheckpoint(fileId: Long): Int =
        checkpointFile(fileId)?.takeIf { it.exists() }?.readText()?.toIntOrNull() ?: 0

    private fun saveCheckpoint(fileId: Long, nextIdx: Int) {
        checkpointFile(fileId)?.writeText(nextIdx.toString())
    }

    private fun clearCheckpoint(fileId: Long) {
        checkpointFile(fileId)?.delete()
    }
}
