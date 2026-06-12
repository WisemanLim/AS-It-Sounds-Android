package com.asitsounds.android.asr

object WhisperJni {

    // Called from AsItSoundsApp.onCreate() — keeps init out of object initializer
    // so parseSegments remains unit-testable without the native .so present.
    fun loadNative() { System.loadLibrary("whisper_jni") }

    private external fun initContext(modelPath: String): Long
    private external fun freeContext(ctxPtr: Long)

    // Returns newline-delimited "startMs|endMs|text|lang" records
    private external fun transcribeNative(ctxPtr: Long, audioData: FloatArray, language: String): String

    fun createContext(modelPath: String): Long = initContext(modelPath)

    fun free(ctx: Long) = freeContext(ctx)

    fun transcribe(ctx: Long, audioData: FloatArray, language: String = ""): List<WhisperSegment> {
        if (ctx == 0L) return emptyList()
        return parseSegments(transcribeNative(ctx, audioData, language))
    }

    internal fun parseSegments(raw: String): List<WhisperSegment> {
        if (raw.isBlank()) return emptyList()
        return raw.trim().lines().mapNotNull { line ->
            val parts = line.split("|", limit = 4)
            if (parts.size < 4) return@mapNotNull null
            WhisperSegment(
                startMs = parts[0].toLongOrNull() ?: return@mapNotNull null,
                endMs   = parts[1].toLongOrNull() ?: return@mapNotNull null,
                text    = parts[2].trim(),
                lang    = parts[3]
            )
        }
    }
}
