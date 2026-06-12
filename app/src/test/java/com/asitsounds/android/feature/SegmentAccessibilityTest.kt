package com.asitsounds.android.feature

import com.asitsounds.android.core.data.db.SegmentEntity
import com.asitsounds.android.core.ui.SegmentAccessibility
import org.junit.Assert.*
import org.junit.Test

class SegmentAccessibilityTest {

    private fun seg(start: Long, en: String, han: String, ko: String) =
        SegmentEntity(id = 1, fileId = 1, idx = 0, startMs = start, endMs = start + 1000,
            textEn = en, textHangul = han, textKo = ko)

    @Test
    fun describe_combinesAllLines() {
        val d = SegmentAccessibility.describe(seg(0, "Hello", "헬로", "안녕"))
        assertEquals("00:00, Hello, 헬로, 안녕", d)
    }

    @Test
    fun describe_skipsBlankLines() {
        val d = SegmentAccessibility.describe(seg(65000, "Hi", "", "안녕"))
        assertEquals("01:05, Hi, 안녕", d)
    }

    @Test
    fun describe_timecodeOnlyWhenAllBlank() {
        val d = SegmentAccessibility.describe(seg(0, "", "", ""))
        assertEquals("00:00", d)
    }

    @Test
    fun timecode_formatsMinutesSeconds() {
        assertEquals("00:00", SegmentAccessibility.timecode(0))
        assertEquals("00:05", SegmentAccessibility.timecode(5000))
        assertEquals("01:05", SegmentAccessibility.timecode(65000))
        assertEquals("10:00", SegmentAccessibility.timecode(600000))
    }
}
