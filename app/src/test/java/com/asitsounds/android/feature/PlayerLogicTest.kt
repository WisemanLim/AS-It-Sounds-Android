package com.asitsounds.android.feature

import com.asitsounds.android.feature.project.PlayerLogic
import com.asitsounds.android.feature.project.SegmentFilter
import org.junit.Assert.*
import org.junit.Test

class PlayerLogicTest {

    private val starts = listOf(0L, 1000L, 2000L)
    private val ends   = listOf(1000L, 2000L, 3000L)

    // --- activeSegmentIndex ---

    @Test
    fun active_withinFirstSegment() {
        assertEquals(0, PlayerLogic.activeSegmentIndex(500L, starts, ends))
    }

    @Test
    fun active_atSegmentBoundary_belongsToNext() {
        // 1000 is start of segment 1 (end of 0 is exclusive)
        assertEquals(1, PlayerLogic.activeSegmentIndex(1000L, starts, ends))
    }

    @Test
    fun active_lastSegment() {
        assertEquals(2, PlayerLogic.activeSegmentIndex(2500L, starts, ends))
    }

    @Test
    fun active_pastEnd_returnsLast() {
        assertEquals(2, PlayerLogic.activeSegmentIndex(9999L, starts, ends))
    }

    @Test
    fun active_emptySegments_returnsMinusOne() {
        assertEquals(-1, PlayerLogic.activeSegmentIndex(100L, emptyList(), emptyList()))
    }

    @Test
    fun active_gapBetweenSegments_returnsPreceding() {
        val gapStarts = listOf(0L, 2000L)
        val gapEnds   = listOf(1000L, 3000L)
        // 1500 is in the gap → preceding segment 0
        assertEquals(0, PlayerLogic.activeSegmentIndex(1500L, gapStarts, gapEnds))
    }

    // --- clampSpeed ---

    @Test
    fun clampSpeed_withinRange_unchanged() {
        assertEquals(1.0f, PlayerLogic.clampSpeed(1.0f), 1e-6f)
        assertEquals(1.5f, PlayerLogic.clampSpeed(1.5f), 1e-6f)
    }

    @Test
    fun clampSpeed_belowMin_clampsTo05() {
        assertEquals(0.5f, PlayerLogic.clampSpeed(0.1f), 1e-6f)
    }

    @Test
    fun clampSpeed_aboveMax_clampsTo2() {
        assertEquals(2.0f, PlayerLogic.clampSpeed(5.0f), 1e-6f)
    }

    // --- seekRelative ---

    @Test
    fun seek_forward() {
        assertEquals(8000L, PlayerLogic.seekRelative(3000L, 5000L, 60000L))
    }

    @Test
    fun seek_backwardClampedToZero() {
        assertEquals(0L, PlayerLogic.seekRelative(3000L, -5000L, 60000L))
    }

    @Test
    fun seek_forwardClampedToDuration() {
        assertEquals(60000L, PlayerLogic.seekRelative(58000L, 5000L, 60000L))
    }

    // --- SegmentFilter visibility ---

    @Test
    fun segmentFilter_all_showsAll() {
        assertTrue(SegmentFilter.ALL.showEn)
        assertTrue(SegmentFilter.ALL.showHangul)
        assertTrue(SegmentFilter.ALL.showKo)
    }

    @Test
    fun segmentFilter_en_onlyEn() {
        assertTrue(SegmentFilter.EN.showEn)
        assertFalse(SegmentFilter.EN.showHangul)
        assertFalse(SegmentFilter.EN.showKo)
    }

    @Test
    fun segmentFilter_hangul_onlyHangul() {
        assertFalse(SegmentFilter.HANGUL.showEn)
        assertTrue(SegmentFilter.HANGUL.showHangul)
        assertFalse(SegmentFilter.HANGUL.showKo)
    }

    @Test
    fun segmentFilter_ko_onlyKo() {
        assertFalse(SegmentFilter.KO.showEn)
        assertFalse(SegmentFilter.KO.showHangul)
        assertTrue(SegmentFilter.KO.showKo)
    }
}
