package com.asitsounds.android.pipeline

import org.junit.Assert.*
import org.junit.Test

class PipelineEventTest {

    @Test
    fun modelDownloading_storesProgress() {
        val e = PipelineEvent.ModelDownloading(42)
        assertEquals(42, e.progressPct)
    }

    @Test
    fun asrProgress_storesPercent() {
        val e = PipelineEvent.AsrProgress(75)
        assertEquals(75, e.progressPct)
    }

    @Test
    fun translatingSegment_storesIndex() {
        val e = PipelineEvent.TranslatingSegment(3)
        assertEquals(3, e.segmentIndex)
    }

    @Test
    fun segment_storesAllFields() {
        val e = PipelineEvent.Segment(
            index = 0, startMs = 100L, endMs = 2000L,
            textEn = "Hello", textHangul = "헬로", textKo = "안녕", lang = "en"
        )
        assertEquals(0, e.index)
        assertEquals(100L, e.startMs)
        assertEquals(2000L, e.endMs)
        assertEquals("Hello", e.textEn)
        assertEquals("헬로", e.textHangul)
        assertEquals("안녕", e.textKo)
        assertEquals("en", e.lang)
    }

    @Test
    fun error_storesCause() {
        val ex = IllegalStateException("oops")
        val e = PipelineEvent.Error(ex)
        assertSame(ex, e.cause)
    }

    @Test
    fun fileDone_isSingletonObject() {
        assertSame(PipelineEvent.FileDone, PipelineEvent.FileDone)
    }

    @Test
    fun decoding_isSingletonObject() {
        assertSame(PipelineEvent.Decoding, PipelineEvent.Decoding)
    }

    @Test
    fun segment_equalityByValue() {
        val a = PipelineEvent.Segment(0, 0L, 1000L, "Hi", "하이", "안녕", "en")
        val b = PipelineEvent.Segment(0, 0L, 1000L, "Hi", "하이", "안녕", "en")
        assertEquals(a, b)
    }
}
