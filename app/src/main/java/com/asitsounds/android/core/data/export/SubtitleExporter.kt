package com.asitsounds.android.core.data.export

import com.asitsounds.android.core.data.db.SegmentEntity

enum class SubtitleFormat(val extension: String, val mime: String) {
    SRT("srt", "application/x-subrip"),
    VTT("vtt", "text/vtt"),
    TXT("txt", "text/plain")
}

// Which text line(s) to include per segment.
enum class ExportContent { EN, HANGUL, KO, EN_HANGUL_KO }

object SubtitleExporter {

    fun export(
        segments: List<SegmentEntity>,
        format: SubtitleFormat,
        content: ExportContent = ExportContent.EN_HANGUL_KO
    ): String = when (format) {
        SubtitleFormat.SRT -> buildSrt(segments, content)
        SubtitleFormat.VTT -> buildVtt(segments, content)
        SubtitleFormat.TXT -> buildTxt(segments, content)
    }

    private fun lines(seg: SegmentEntity, content: ExportContent): String = when (content) {
        ExportContent.EN     -> seg.textEn
        ExportContent.HANGUL -> seg.textHangul
        ExportContent.KO     -> seg.textKo
        ExportContent.EN_HANGUL_KO ->
            listOf(seg.textEn, seg.textHangul, seg.textKo)
                .filter { it.isNotBlank() }
                .joinToString("\n")
    }

    private fun buildSrt(segments: List<SegmentEntity>, content: ExportContent): String =
        buildString {
            segments.forEachIndexed { i, seg ->
                append(i + 1).append('\n')
                append(srtTime(seg.startMs)).append(" --> ").append(srtTime(seg.endMs)).append('\n')
                append(lines(seg, content)).append("\n\n")
            }
        }.trimEnd('\n') + "\n"

    private fun buildVtt(segments: List<SegmentEntity>, content: ExportContent): String =
        buildString {
            append("WEBVTT\n\n")
            segments.forEach { seg ->
                append(vttTime(seg.startMs)).append(" --> ").append(vttTime(seg.endMs)).append('\n')
                append(lines(seg, content)).append("\n\n")
            }
        }.trimEnd('\n') + "\n"

    private fun buildTxt(segments: List<SegmentEntity>, content: ExportContent): String =
        segments.joinToString("\n\n") { lines(it, content) } + "\n"

    // SRT uses comma decimal separator: HH:MM:SS,mmm
    internal fun srtTime(ms: Long): String = formatTime(ms, ',')

    // VTT uses dot decimal separator: HH:MM:SS.mmm
    internal fun vttTime(ms: Long): String = formatTime(ms, '.')

    private fun formatTime(ms: Long, sep: Char): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        val millis = ms % 1000
        return "%02d:%02d:%02d%c%03d".format(h, m, s, sep, millis)
    }
}
