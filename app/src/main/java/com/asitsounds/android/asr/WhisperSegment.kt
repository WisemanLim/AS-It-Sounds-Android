package com.asitsounds.android.asr

data class WhisperSegment(
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val lang: String
)
