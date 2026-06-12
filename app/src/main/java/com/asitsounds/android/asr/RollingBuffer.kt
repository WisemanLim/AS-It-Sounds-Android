package com.asitsounds.android.asr

// Fixed-capacity ring buffer for real-time STT — keeps the most recent N samples
// (e.g. 5 seconds @ 16kHz = 80_000 samples). Float32 PCM.
class RollingBuffer(val capacity: Int) {
    private val data = FloatArray(capacity)
    private var head = 0      // next write position
    private var filled = 0    // valid sample count

    val size: Int get() = filled
    val isFull: Boolean get() = filled == capacity

    fun append(samples: FloatArray) {
        for (s in samples) {
            data[head] = s
            head = (head + 1) % capacity
            if (filled < capacity) filled++
        }
    }

    // Snapshot in chronological order (oldest → newest).
    fun snapshot(): FloatArray {
        val out = FloatArray(filled)
        val start = if (filled < capacity) 0 else head
        for (i in 0 until filled) {
            out[i] = data[(start + i) % capacity]
        }
        return out
    }

    fun clear() {
        head = 0
        filled = 0
    }

    companion object {
        const val SAMPLE_RATE = 16_000
        const val WINDOW_SECONDS = 5
        fun forWindow(seconds: Int = WINDOW_SECONDS): RollingBuffer =
            RollingBuffer(SAMPLE_RATE * seconds)
    }
}
