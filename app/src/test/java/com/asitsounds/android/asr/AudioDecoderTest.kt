package com.asitsounds.android.asr

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioDecoderTest {

    // --- pcm16ToFloat ---

    @Test
    fun pcm16ToFloat_zero_returns_zero() {
        val bytes = ByteArray(2) // 0x0000 = 0
        val result = AudioDecoder.pcm16ToFloat(bytes)
        assertEquals(1, result.size)
        assertEquals(0f, result[0], 1e-6f)
    }

    @Test
    fun pcm16ToFloat_maxPositive_approaches_one() {
        // 0x7FFF = 32767
        val bytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(32767).array()
        val result = AudioDecoder.pcm16ToFloat(bytes)
        assertEquals(1, result.size)
        assertTrue("Expected near 1.0 but got ${result[0]}", result[0] > 0.999f)
    }

    @Test
    fun pcm16ToFloat_minNegative_approaches_negative_one() {
        // -32768 / 32768 = -1.0
        val bytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(-32768).array()
        val result = AudioDecoder.pcm16ToFloat(bytes)
        assertEquals(1, result.size)
        assertEquals(-1f, result[0], 1e-6f)
    }

    @Test
    fun pcm16ToFloat_multiSample_correctCount() {
        val bytes = ByteArray(8) // 4 samples × 2 bytes
        val result = AudioDecoder.pcm16ToFloat(bytes)
        assertEquals(4, result.size)
    }

    // --- stereoToMono ---

    @Test
    fun stereoToMono_averagesChannelPairs() {
        val stereo = floatArrayOf(1f, -1f, 0.5f, 0.5f)
        val mono = AudioDecoder.stereoToMono(stereo)
        assertEquals(2, mono.size)
        assertEquals(0f, mono[0], 1e-6f)    // (1 + -1) / 2
        assertEquals(0.5f, mono[1], 1e-6f)  // (0.5 + 0.5) / 2
    }

    @Test
    fun stereoToMono_identicalChannels_unchanged() {
        val stereo = floatArrayOf(0.3f, 0.3f, -0.7f, -0.7f)
        val mono = AudioDecoder.stereoToMono(stereo)
        assertEquals(0.3f, mono[0], 1e-6f)
        assertEquals(-0.7f, mono[1], 1e-6f)
    }

    // --- resample ---

    @Test
    fun resample_sameRate_returnsOriginal() {
        val input = floatArrayOf(0.1f, 0.2f, 0.3f)
        val result = AudioDecoder.resample(input, 16000, 16000)
        assertArrayEquals(input, result, 1e-6f)
    }

    @Test
    fun resample_empty_returnsEmpty() {
        val result = AudioDecoder.resample(floatArrayOf(), 44100, 16000)
        assertEquals(0, result.size)
    }

    @Test
    fun resample_44100_to_16000_correctOutputLength() {
        val input = FloatArray(44100) { it.toFloat() } // 1 second at 44100
        val result = AudioDecoder.resample(input, 44100, 16000)
        // expected: ~16000 samples
        assertEquals(16000, result.size)
    }

    @Test
    fun resample_constantSignal_remainsConstant() {
        val input = FloatArray(441) { 0.5f }
        val result = AudioDecoder.resample(input, 44100, 16000)
        for (sample in result) {
            assertEquals(0.5f, sample, 1e-5f)
        }
    }

    // --- constants ---

    @Test
    fun targetSampleRate_is16kHz() {
        assertEquals(16_000, AudioDecoder.TARGET_SAMPLE_RATE)
    }

    @Test
    fun targetChannels_isMono() {
        assertEquals(1, AudioDecoder.TARGET_CHANNELS)
    }
}
