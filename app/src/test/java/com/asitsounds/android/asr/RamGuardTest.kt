package com.asitsounds.android.asr

import org.junit.Assert.assertEquals
import org.junit.Test

class RamGuardTest {

    private val GB = 1024L * 1024 * 1024

    @Test
    fun below2GB_recommendsTiny() {
        assertEquals(WhisperModel.TINY, RamGuard.modelForRam(1 * GB))
    }

    @Test
    fun exactly2GB_recommendsTiny() {
        assertEquals(WhisperModel.TINY, RamGuard.modelForRam(2 * GB))
    }

    @Test
    fun just_above_2GB_recommendsTiny() {
        assertEquals(WhisperModel.TINY, RamGuard.modelForRam(2 * GB + 1))
    }

    @Test
    fun exactly3GB_recommendsBase() {
        assertEquals(WhisperModel.BASE, RamGuard.modelForRam(3 * GB))
    }

    @Test
    fun between3and6GB_recommendsBase() {
        assertEquals(WhisperModel.BASE, RamGuard.modelForRam(4 * GB))
        assertEquals(WhisperModel.BASE, RamGuard.modelForRam(5 * GB))
        assertEquals(WhisperModel.BASE, RamGuard.modelForRam(6 * GB - 1))
    }

    @Test
    fun exactly6GB_recommendsSmall() {
        assertEquals(WhisperModel.SMALL, RamGuard.modelForRam(6 * GB))
    }

    @Test
    fun above6GB_recommendsSmall() {
        assertEquals(WhisperModel.SMALL, RamGuard.modelForRam(8 * GB))
        assertEquals(WhisperModel.SMALL, RamGuard.modelForRam(12 * GB))
    }
}
