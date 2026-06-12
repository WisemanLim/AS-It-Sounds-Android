package com.asitsounds.android.platform

import com.asitsounds.android.asr.RollingBuffer
import org.junit.Assert.*
import org.junit.Test

class RollingBufferTest {

    @Test
    fun empty_sizeZero() {
        val b = RollingBuffer(10)
        assertEquals(0, b.size)
        assertEquals(0, b.snapshot().size)
        assertFalse(b.isFull)
    }

    @Test
    fun append_belowCapacity_keepsAll() {
        val b = RollingBuffer(10)
        b.append(floatArrayOf(1f, 2f, 3f))
        assertEquals(3, b.size)
        assertArrayEquals(floatArrayOf(1f, 2f, 3f), b.snapshot(), 1e-6f)
    }

    @Test
    fun append_exactlyCapacity_isFull() {
        val b = RollingBuffer(3)
        b.append(floatArrayOf(1f, 2f, 3f))
        assertTrue(b.isFull)
        assertArrayEquals(floatArrayOf(1f, 2f, 3f), b.snapshot(), 1e-6f)
    }

    @Test
    fun append_overCapacity_keepsNewest() {
        val b = RollingBuffer(3)
        b.append(floatArrayOf(1f, 2f, 3f, 4f, 5f))
        assertEquals(3, b.size)
        // newest 3 in chronological order
        assertArrayEquals(floatArrayOf(3f, 4f, 5f), b.snapshot(), 1e-6f)
    }

    @Test
    fun append_multipleWraps_chronologicalOrder() {
        val b = RollingBuffer(4)
        b.append(floatArrayOf(1f, 2f))
        b.append(floatArrayOf(3f, 4f, 5f, 6f))
        assertArrayEquals(floatArrayOf(3f, 4f, 5f, 6f), b.snapshot(), 1e-6f)
    }

    @Test
    fun clear_resets() {
        val b = RollingBuffer(3)
        b.append(floatArrayOf(1f, 2f, 3f))
        b.clear()
        assertEquals(0, b.size)
        assertFalse(b.isFull)
    }

    @Test
    fun forWindow_sizesToSampleRate() {
        val b = RollingBuffer.forWindow(5)
        assertEquals(16_000 * 5, b.capacity)
    }

    @Test
    fun forWindow_default5Seconds() {
        val b = RollingBuffer.forWindow()
        assertEquals(80_000, b.capacity)
    }
}
