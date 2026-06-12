package com.asitsounds.android.core.data

import com.asitsounds.android.core.data.db.SegmentEntity
import com.asitsounds.android.core.data.export.ExportContent
import com.asitsounds.android.core.data.export.SubtitleExporter
import com.asitsounds.android.core.data.export.SubtitleFormat
import org.junit.Assert.*
import org.junit.Test

class SubtitleExporterTest {

    private fun seg(idx: Int, start: Long, end: Long, en: String, han: String, ko: String) =
        SegmentEntity(id = idx.toLong(), fileId = 1, idx = idx,
            startMs = start, endMs = end, textEn = en, textHangul = han, textKo = ko)

    private val sample = listOf(
        seg(0, 0L, 1500L, "Hello", "헬로", "안녕"),
        seg(1, 1500L, 3200L, "World", "월드", "세계")
    )

    // --- time formatting ---

    @Test
    fun srtTime_formatsWithComma() {
        assertEquals("00:00:00,000", SubtitleExporter.srtTime(0L))
        assertEquals("00:00:01,500", SubtitleExporter.srtTime(1500L))
        assertEquals("01:01:01,001", SubtitleExporter.srtTime(3_661_001L))
    }

    @Test
    fun vttTime_formatsWithDot() {
        assertEquals("00:00:00.000", SubtitleExporter.vttTime(0L))
        assertEquals("00:00:03.200", SubtitleExporter.vttTime(3200L))
    }

    @Test
    fun srtTime_handlesHours() {
        // 2h 3m 4s 5ms
        val ms = (2L * 3600 + 3 * 60 + 4) * 1000 + 5
        assertEquals("02:03:04,005", SubtitleExporter.srtTime(ms))
    }

    // --- SRT ---

    @Test
    fun srt_hasSequentialIndices() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.SRT, ExportContent.EN)
        val lines = out.lines()
        assertEquals("1", lines[0])
        // second cue index appears later
        assertTrue(out.contains("\n2\n"))
    }

    @Test
    fun srt_hasArrowTimecodes() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.SRT, ExportContent.EN)
        assertTrue(out.contains("00:00:00,000 --> 00:00:01,500"))
        assertTrue(out.contains("00:00:01,500 --> 00:00:03,200"))
    }

    @Test
    fun srt_includesText() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.SRT, ExportContent.EN)
        assertTrue(out.contains("Hello"))
        assertTrue(out.contains("World"))
    }

    @Test
    fun srt_threeLineContent_stacksAllLines() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.SRT, ExportContent.EN_HANGUL_KO)
        assertTrue(out.contains("Hello\n헬로\n안녕"))
    }

    // --- VTT ---

    @Test
    fun vtt_startsWithHeader() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.VTT, ExportContent.EN)
        assertTrue(out.startsWith("WEBVTT"))
    }

    @Test
    fun vtt_hasDotTimecodes() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.VTT, ExportContent.KO)
        assertTrue(out.contains("00:00:00.000 --> 00:00:01.500"))
    }

    @Test
    fun vtt_noNumericIndices() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.VTT, ExportContent.EN)
        // VTT cues are not numbered like SRT; first content line after header is a timecode
        val afterHeader = out.removePrefix("WEBVTT").trimStart()
        assertTrue("VTT should start cue with timecode", afterHeader.startsWith("00:"))
    }

    // --- TXT ---

    @Test
    fun txt_koOnly_joinsText() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.TXT, ExportContent.KO)
        assertTrue(out.contains("안녕"))
        assertTrue(out.contains("세계"))
        assertFalse("TXT should not contain timecodes", out.contains("-->"))
    }

    @Test
    fun txt_hangulOnly() {
        val out = SubtitleExporter.export(sample, SubtitleFormat.TXT, ExportContent.HANGUL)
        assertTrue(out.contains("헬로"))
        assertFalse(out.contains("Hello"))
    }

    // --- content filtering ---

    @Test
    fun content_enHangulKo_skipsBlankLines() {
        val withBlank = listOf(seg(0, 0L, 1000L, "Hi", "", "안녕"))
        val out = SubtitleExporter.export(withBlank, SubtitleFormat.TXT, ExportContent.EN_HANGUL_KO)
        // empty hangul line should not create a double-blank
        assertEquals("Hi\n안녕\n", out)
    }

    // --- format metadata ---

    @Test
    fun formats_haveCorrectExtensions() {
        assertEquals("srt", SubtitleFormat.SRT.extension)
        assertEquals("vtt", SubtitleFormat.VTT.extension)
        assertEquals("txt", SubtitleFormat.TXT.extension)
    }

    @Test
    fun emptyList_srt_producesEmptyish() {
        val out = SubtitleExporter.export(emptyList(), SubtitleFormat.SRT, ExportContent.EN)
        assertFalse(out.contains("-->"))
    }

    @Test
    fun emptyList_vtt_stillHasHeader() {
        val out = SubtitleExporter.export(emptyList(), SubtitleFormat.VTT, ExportContent.EN)
        assertTrue(out.startsWith("WEBVTT"))
    }
}
