package com.asitsounds.android.asr

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioDecoder {

    companion object {
        const val TARGET_SAMPLE_RATE = 16_000
        const val TARGET_CHANNELS = 1
        private const val TIMEOUT_US = 10_000L

        // Convert little-endian PCM-16 bytes to Float32 in [-1, 1]
        internal fun pcm16ToFloat(bytes: ByteArray): FloatArray {
            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            return FloatArray(buf.remaining()) { buf.get() / 32768f }
        }

        // Down-mix interleaved stereo to mono by averaging channel pairs
        internal fun stereoToMono(stereo: FloatArray): FloatArray {
            val mono = FloatArray(stereo.size / 2)
            for (i in mono.indices) {
                mono[i] = (stereo[i * 2] + stereo[i * 2 + 1]) * 0.5f
            }
            return mono
        }

        // Linear interpolation resample
        internal fun resample(samples: FloatArray, srcRate: Int, dstRate: Int): FloatArray {
            if (srcRate == dstRate || samples.isEmpty()) return samples
            val ratio = srcRate.toDouble() / dstRate
            val outLen = (samples.size / ratio).toInt()
            return FloatArray(outLen) { i ->
                val pos = i * ratio
                val lo = pos.toInt().coerceAtMost(samples.lastIndex)
                val hi = (lo + 1).coerceAtMost(samples.lastIndex)
                val frac = (pos - lo).toFloat()
                samples[lo] * (1f - frac) + samples[hi] * frac
            }
        }
    }

    @Throws(IOException::class)
    suspend fun decode(context: Context, uri: Uri): FloatArray = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)

        val trackIndex = (0 until extractor.trackCount).firstOrNull { idx ->
            extractor.getTrackFormat(idx).getString(MediaFormat.KEY_MIME)
                ?.startsWith("audio/") == true
        } ?: throw IOException("No audio track in: $uri")

        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME)!!
        val srcSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val srcChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val out = ByteArrayOutputStream()
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        while (!outputDone) {
            if (!inputDone) {
                val inIdx = codec.dequeueInputBuffer(TIMEOUT_US)
                if (inIdx >= 0) {
                    val inBuf: ByteBuffer = codec.getInputBuffer(inIdx)!!
                    val n = extractor.readSampleData(inBuf, 0)
                    if (n < 0) {
                        codec.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(inIdx, 0, n, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }
            val outIdx = codec.dequeueOutputBuffer(info, TIMEOUT_US)
            if (outIdx >= 0) {
                val outBuf: ByteBuffer = codec.getOutputBuffer(outIdx)!!
                val chunk = ByteArray(info.size)
                outBuf.get(chunk)
                out.write(chunk)
                codec.releaseOutputBuffer(outIdx, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
            }
        }

        codec.stop()
        codec.release()
        extractor.release()

        var samples = pcm16ToFloat(out.toByteArray())
        if (srcChannels > 1) samples = stereoToMono(samples)
        if (srcSampleRate != TARGET_SAMPLE_RATE) samples = resample(samples, srcSampleRate, TARGET_SAMPLE_RATE)
        samples
    }
}
