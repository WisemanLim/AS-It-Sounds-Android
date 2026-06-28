package com.asitsounds.android.pipeline

sealed class PipelineEvent {
    data class ModelDownloading(val progressPct: Int) : PipelineEvent()
    data object Decoding : PipelineEvent()
    data class AsrProgress(val progressPct: Int) : PipelineEvent()
    data class TranslatingSegment(val segmentIndex: Int) : PipelineEvent()
    data class TranslationProgress(val progressPct: Int, val segmentIndex: Int, val total: Int) : PipelineEvent()
    data class Segment(
        val index: Int,
        val startMs: Long,
        val endMs: Long,
        val textEn: String,
        val textHangul: String,
        val textKo: String,
        val lang: String
    ) : PipelineEvent()
    data object FileDone : PipelineEvent()
    data class Error(val cause: Throwable) : PipelineEvent()
}
