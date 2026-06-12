package com.asitsounds.android.platform

import com.asitsounds.android.pipeline.TranscriptionWorker
import org.junit.Assert.*
import org.junit.Test

class PlatformLogicTest {

    // --- NotificationHelper ---

    @Test
    fun progressText_zeroTotal_preparing() {
        assertEquals("준비 중…", NotificationHelper.progressText(0, 0))
    }

    @Test
    fun progressText_withTotal() {
        assertEquals("변환 중 3/10 세그먼트", NotificationHelper.progressText(3, 10))
    }

    @Test
    fun progressPercent_zeroTotal_isZero() {
        assertEquals(0, NotificationHelper.progressPercent(5, 0))
    }

    @Test
    fun progressPercent_half() {
        assertEquals(50, NotificationHelper.progressPercent(5, 10))
    }

    @Test
    fun progressPercent_full() {
        assertEquals(100, NotificationHelper.progressPercent(10, 10))
    }

    @Test
    fun progressPercent_clampedTo100() {
        assertEquals(100, NotificationHelper.progressPercent(15, 10))
    }

    // --- AppShortcuts ---

    @Test
    fun shortcutId_format() {
        assertEquals("project_42", AppShortcuts.shortcutId(42))
    }

    @Test
    fun projectIdFromShortcut_roundTrip() {
        assertEquals(42L, AppShortcuts.projectIdFromShortcut(AppShortcuts.shortcutId(42)))
    }

    @Test
    fun projectIdFromShortcut_invalid_null() {
        assertNull(AppShortcuts.projectIdFromShortcut("something_else"))
        assertNull(AppShortcuts.projectIdFromShortcut(null))
    }

    @Test
    fun maxShortcuts_isFour() {
        assertEquals(4, AppShortcuts.MAX_SHORTCUTS)
    }

    // --- TranscriptionWorker work name ---

    @Test
    fun workName_perFile() {
        assertEquals("transcribe_7", TranscriptionWorker.workName(7))
    }

    @Test
    fun workName_unique_perFileId() {
        assertNotEquals(TranscriptionWorker.workName(1), TranscriptionWorker.workName(2))
    }
}
