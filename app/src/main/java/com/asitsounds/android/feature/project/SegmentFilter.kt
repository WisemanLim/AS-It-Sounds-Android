package com.asitsounds.android.feature.project

enum class SegmentFilter(val label: String) {
    ALL("모두"),
    EN("영문"),
    HANGUL("한글발음"),
    KO("번역");

    val showEn: Boolean get() = this == ALL || this == EN
    val showHangul: Boolean get() = this == ALL || this == HANGUL
    val showKo: Boolean get() = this == ALL || this == KO
}

object PlayerLogic {
    // Which segment index is active for a given playback position (ms).
    fun activeSegmentIndex(positionMs: Long, starts: List<Long>, ends: List<Long>): Int {
        for (i in starts.indices) {
            if (positionMs in starts[i] until ends[i]) return i
        }
        // before first / in gaps → nearest preceding segment, else -1
        var best = -1
        for (i in starts.indices) {
            if (starts[i] <= positionMs) best = i else break
        }
        return best
    }

    // Clamp playback speed to supported 0.5x..2.0x range.
    fun clampSpeed(speed: Float): Float = speed.coerceIn(0.5f, 2.0f)

    // ±5s seek bounded to [0, duration].
    fun seekRelative(currentMs: Long, deltaMs: Long, durationMs: Long): Long =
        (currentMs + deltaMs).coerceIn(0L, durationMs)
}
