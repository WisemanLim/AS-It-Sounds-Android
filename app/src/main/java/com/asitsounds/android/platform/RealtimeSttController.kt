package com.asitsounds.android.platform

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.asitsounds.android.asr.RollingBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

// Captures mic audio into a 5s rolling buffer, emitting snapshots for whisper.cpp inference.
// Requires RECORD_AUDIO permission (caller must check).
class RealtimeSttController(
    private val sampleRate: Int = RollingBuffer.SAMPLE_RATE,
    private val emitIntervalMs: Long = 1000L
) {
    private val buffer = RollingBuffer.forWindow()

    @SuppressLint("MissingPermission")
    fun start(): Flow<FloatArray> = flow {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            minBuf.coerceAtLeast(sampleRate)
        )
        val chunk = FloatArray(sampleRate / 4) // ~250ms
        try {
            record.startRecording()
            var sinceEmit = 0L
            while (true) {
                val n = record.read(chunk, 0, chunk.size, AudioRecord.READ_BLOCKING)
                if (n > 0) {
                    buffer.append(chunk.copyOf(n))
                    sinceEmit += (n * 1000L / sampleRate)
                    if (sinceEmit >= emitIntervalMs) {
                        emit(buffer.snapshot())
                        sinceEmit = 0L
                    }
                }
            }
        } finally {
            record.stop()
            record.release()
        }
    }.flowOn(Dispatchers.IO)
}
