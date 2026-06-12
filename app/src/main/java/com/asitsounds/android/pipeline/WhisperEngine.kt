package com.asitsounds.android.pipeline

import com.asitsounds.android.asr.WhisperJni
import com.asitsounds.android.asr.WhisperSegment

interface WhisperEngine {
    fun transcribe(audioData: FloatArray, language: String = ""): List<WhisperSegment>
    fun close() {}
}

class WhisperJniEngine(modelPath: String) : WhisperEngine {
    private val ctx: Long by lazy { WhisperJni.createContext(modelPath) }

    override fun transcribe(audioData: FloatArray, language: String): List<WhisperSegment> =
        WhisperJni.transcribe(ctx, audioData, language)

    override fun close() {
        if (ctx != 0L) WhisperJni.free(ctx)
    }
}
