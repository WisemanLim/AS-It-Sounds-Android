package com.asitsounds.android.core.ui

import com.asitsounds.android.core.data.db.SegmentEntity

// Builds a combined TalkBack contentDescription for a 3-line segment row
// (timecode + EN + Hangul + KO), per PRD §5 accessibility acceptance criterion.
object SegmentAccessibility {

    fun describe(seg: SegmentEntity): String {
        val parts = mutableListOf(timecode(seg.startMs))
        if (seg.textEn.isNotBlank()) parts += seg.textEn
        if (seg.textHangul.isNotBlank()) parts += seg.textHangul
        if (seg.textKo.isNotBlank()) parts += seg.textKo
        return parts.joinToString(", ")
    }

    fun timecode(ms: Long): String {
        val totalSec = ms / 1000
        return "%02d:%02d".format(totalSec / 60, totalSec % 60)
    }
}
